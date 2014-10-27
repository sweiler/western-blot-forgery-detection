package forgery.web.client;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import forgery.util.Config;
import forgery.util.MatchPair;
import forgery.util.Utility;

public class ClientMain implements Runnable {

	private CloseableHttpClient httpClient;
	private boolean done = false;
	private static String BASE_URL;
	private Config config;
	private static int SOFTWARE_VERSION = 1;

	private void init() throws KeyStoreException, NoSuchAlgorithmException,
			KeyManagementException, CertificateException, IOException,
			UnrecoverableKeyException {

		config = Utility.instance().getConfiguration();
		BASE_URL = config.baseUrl.trim();

		KeyStore keystore = KeyStore.getInstance("jks");
		FileInputStream fis = new FileInputStream("keystore.jks");

		keystore.load(fis, "Yohdoh2t".toCharArray());

		KeyStore clientKeystore = KeyStore.getInstance("jks");
		fis = new FileInputStream("clientcert.jks");

		clientKeystore.load(fis, "phaiVe5o".toCharArray());

		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(keystore);
		builder.loadKeyMaterial(clientKeystore, "phaiVe5o".toCharArray());
		X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
				builder.build(), hostnameVerifier);

		httpClient = HttpClients.custom().setSSLSocketFactory(sslSocketFactory)
				.build();
	}

	private int pullTask() throws ClientProtocolException, IOException,
			ParseException {
		HttpGet get = new HttpGet(BASE_URL + "/tasks");
		CloseableHttpResponse resp = httpClient.execute(get);
		int task = -1;
		try {
			HttpEntity entity = resp.getEntity();

			StringWriter sw = new StringWriter();
			IOUtils.copy(entity.getContent(), sw);
			System.out.println(sw.toString());

			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(sw.toString());
			String status = (String) obj.get("status");

			if (status.equals("ok")) {
				JSONArray tasks = (JSONArray) obj.get("tasks");
				task = ((Long) tasks.get(0)).intValue();
				System.out.println("Claim task " + task);

			}

			EntityUtils.consume(entity);

		} finally {
			resp.close();
		}
		return task;
	}

	private List<Rectangle> extractRects(JSONObject task) {
		List<Rectangle> result = new ArrayList<>();

		JSONArray rects = (JSONArray) task.get("rects");

		for (Object o : rects) {
			JSONObject rect = (JSONObject) o;
			int x = ((Long) rect.get("x")).intValue();
			int y = ((Long) rect.get("y")).intValue();
			int width = ((Long) rect.get("width")).intValue();
			int height = ((Long) rect.get("height")).intValue();

			result.add(new Rectangle(x, y, width, height));
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private void processTask(int taskId) throws ClientProtocolException,
			IOException, ParseException {
		HttpGet get = new HttpGet(BASE_URL + "/tasks/" + taskId);
		CloseableHttpResponse resp = httpClient.execute(get);

		try {
			HttpEntity entity = resp.getEntity();

			StringWriter sw = new StringWriter();
			IOUtils.copy(entity.getContent(), sw);

			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(sw.toString());
			String status = (String) obj.get("status");

			JSONObject task = (JSONObject) obj.get("task");

			if (status.equals("ok")) {
				System.out.println("Type: " + task.get("type"));
				List<Rectangle> rects = extractRects(task);
				byte[] filedata = Base64
						.decodeBase64((String) task.get("data"));

				BufferedImage img = ImageIO
						.read(new ByteArrayInputStream(filedata));

				// First pass: focus on background copies
				config.nn_distance = 50;
				// very low range threshold: only complete monotonous blocks are
				// excluded
				config.range_threshold = 0;
				config.avg_intensity_threshold = 0;


				double[] features = Utility.instance().getZernikeFeatures(img,
						rects, config);
				Set<MatchPair> matches = Utility.instance().matchFeatures(
						features, config);
				matches = Utility.instance().filterMatches(matches);

				List<MatchPair> clusters = Utility.instance().clusteredMatches(
						img.getWidth(), img.getHeight(), matches);

				JSONObject answer = new JSONObject();

				answer.put("status", "ok");
				answer.put("version", SOFTWARE_VERSION);
				answer.put("parameters", generateParameters(rects));
				JSONArray clusterRects = new JSONArray();

				for (MatchPair p : clusters) {
					JSONObject pair = new JSONObject();
					pair.put("first", wrapRect(p.getFirst()));
					pair.put("second", wrapRect(p.getSecond()));

					clusterRects.add(pair);
				}

				answer.put("pairs", clusterRects);

				HttpPut put = new HttpPut(BASE_URL + "/tasks/" + taskId);
				put.setEntity(new StringEntity(answer.toJSONString()));
				httpClient.execute(put);

				// done = true;

			} else {
				System.err.println("Task not available: " + obj.get("message"));
			}

			EntityUtils.consume(entity);

		} finally {
			resp.close();
		}
	}

	@SuppressWarnings("unchecked")
	private String generateParameters(List<Rectangle> regionsOfInterest) {
		JSONObject params = new JSONObject();
		String[] relevantParameters = { "nn_distance", "range_threshold",
				"avg_intensity_threshold", "block_size", "knn" };

		Class<?> configClass = config.getClass();
		for (String param : relevantParameters) {
			

			try {

				Field paramField = configClass.getField(param);
				params.put(param, paramField.get(config));

			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException | IllegalAccessException e) {
				System.out.println("Invalid param " + param);
			}

		}
		
		JSONArray rects = new JSONArray();
		for(Rectangle r : regionsOfInterest) {
			rects.add(wrapRect(r));
		}
		params.put("regionsOfInterest", rects);

		return params.toJSONString();
	}

	private JSONObject wrapRect(Rectangle r) {
		Map<String, Integer> rect = new HashMap<>();
		rect.put("x", r.x);
		rect.put("y", r.y);
		rect.put("width", r.width);
		rect.put("height", r.height);
		return new JSONObject(rect);
	}

	@Override
	public void run() {
		try {
			init();
			while (!done) {
				try {
					int task = pullTask();
					if (task == -1) {
						System.err.println("Error, task was -1");
						return;
					}

					processTask(task);

				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.err.println("FATAL ERROR");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ClientMain client = new ClientMain();
		Thread clientThread = new Thread(client);
		clientThread.start();
	}

}
