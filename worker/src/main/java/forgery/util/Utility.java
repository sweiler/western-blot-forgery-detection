package forgery.util;

import static jcuda.driver.JCudaDriver.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import flann.Flann;
import flann.FlannAlgorithmType;
import flann.FlannCentersInitType;
import flann.FlannDistanceType;
import flann.FlannLogLevelType;


public class Utility {

	private static Utility _instance;

	public static Utility instance() {
		if (_instance == null) {
			_instance = new Utility();
		}
		return _instance;
	}

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private File configFile = new File("configuration.xml");

	private Logger log = LogManager.getLogger("Utility");

	private Utility() {

	}

	public Config getConfiguration() {
		try {
			JAXBContext ctx = JAXBContext.newInstance(Config.class);

			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			Config c = (Config) unmarshaller.unmarshal(configFile);
			log.info("Config-File successfully read");
			return c;
		} catch (JAXBException e) {
			log.error("The Config-File has some error!");
			throw new Error("Error on parsing Config-File", e);
		}

	}

	public BufferedImage convertToGreyscale(BufferedImage input) {
		BufferedImage img = new BufferedImage(input.getWidth(),
				input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				int rgb = input.getRGB(x, y);
				int r = rgb >> 16 & 0xFF;
				int g = rgb >> 8 & 0xFF;
				int b = rgb & 0xFF;
				int val = (int) (0.2989 * r + 0.5870 * g + 0.1140 * b);
				rgb = val << 16 | val << 8 | val;
				img.setRGB(x, y, rgb);
			}
		}
		return img;
	}

	public BufferedImage imread(String filename) throws IOException {
		return ImageIO.read(new File(filename));
	}
	
	public void imshow(BufferedImage img) {
		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(img));
		frame.getContentPane().add(label);
		frame.setTitle("imshow");
		frame.setSize(1000, 600);
		frame.setVisible(true);
	}
	
	public List<Rectangle> getRegionsOfInterest(BufferedImage img, Config config) {
		log.info("Regions of interest requested");
		ArrayList<Rectangle> regions = new ArrayList<Rectangle>();

		Mat loaded = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC1);
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				loaded.put(y, x, img.getRaster().getSample(x, y, 0));
			}
		}

		Imgproc.threshold(loaded, loaded, 245, 255, Imgproc.THRESH_BINARY_INV);
		
		Highgui.imwrite("threshold.png", loaded);

		Mat holes = loaded.clone();

		Scalar fillColor = new Scalar(255, 255, 255, 1);

		Mat mask = new Mat();

		Imgproc.floodFill(holes, mask, new Point(0, 0), fillColor);
		Imgproc.floodFill(holes, mask, new Point(img.getWidth() - 1, 0), fillColor);
		Imgproc.floodFill(holes, mask, new Point(img.getWidth() - 1, img.getHeight() - 1), fillColor);
		Imgproc.floodFill(holes, mask, new Point(0, img.getHeight() - 1), fillColor);
		
		Core.bitwise_not(holes, holes);
		
		Highgui.imwrite("floodfill.png", holes);

		Mat white = loaded.clone();

		int width = loaded.width();
		int height = loaded.height();

		Core.rectangle(white, new Point(0, 0), new Point(width, height),
				fillColor, Core.FILLED);

		white.copyTo(loaded, holes);
		
		Highgui.imwrite("filled.png", loaded);

		List<MatOfPoint> contours = new ArrayList<>();

		Imgproc.findContours(loaded, contours, new Mat(),
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (MatOfPoint mop : contours) {
			Rect rect = Imgproc.boundingRect(mop);

			int w = rect.width - 8;
			int h = rect.height - 8;
			int x = rect.x + 4;
			int y = rect.y + 4;
			
			Core.rectangle(loaded, new Point(x, y), new Point(x + w, y + h),
					new Scalar(100));

			if (w < config.threshold_width * width
					|| h < config.threshold_height * height
					|| (w * h) < config.threshold_sum * height * width)
				continue;

			Rectangle r = new Rectangle(x, y, w, h);
			boolean intersects = false;
			Rectangle removal = null;
			// Remove intersections
			for (Rectangle o : regions) {
				if (o.intersects(r)) {
					if(r.width * r.height > o.width * o.height)
						removal = o;
					else
						intersects = true;
					break;
				}
			}
			
			if(removal != null)
				regions.remove(removal);

			if (!intersects)
				regions.add(r);

		}
		
		Highgui.imwrite("rects.png", loaded);

		if (regions.size() == 0) {
			log.warn("No areas detected, using complete image");
			regions.add(new Rectangle(0, 0, width, height));
		}

		log.info("Regions of interest successfully computed");

		return regions;
	}

	private void drawRect(Mat img, Scalar color, Rectangle r) {
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.x + r.width;
		int y2 = r.y + r.height;
		Core.rectangle(img, new Point(x1, y1), new Point(x2, y2), color,
				Core.FILLED);
	}

	public List<MatchPair> clusteredMatches(int width, int height,
			Collection<MatchPair> matches) {
		List<MatchPair> list = new ArrayList<>();
		Mat binary = new Mat(height, width, CvType.CV_8UC1);
		Scalar fillColor = new Scalar(255, 255, 255, 1);
		Scalar black = new Scalar(0, 0, 0, 1);
		Core.rectangle(binary, new Point(0, 0), new Point(width, height),
				black, Core.FILLED);

		for (MatchPair p : matches) {
			drawRect(binary, fillColor, p.getFirst());
			drawRect(binary, fillColor, p.getSecond());
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File("matches.dat"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(matches);
			oos.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

		Highgui.imwrite("test.png", binary);

		List<MatOfPoint> contours = new ArrayList<>();

		Imgproc.findContours(binary, contours, new Mat(),
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (MatOfPoint mop : contours) {
			Rect rect = Imgproc.boundingRect(mop);
			if (rect.area() < 1300)
				continue;
			Rect secondRect = null;
			for (MatchPair pair : matches) {
				Point center1 = new Point(pair.getFirst().getCenterX(), pair
						.getFirst().getCenterY());
				Point center2 = new Point(pair.getSecond().getCenterX(), pair
						.getSecond().getCenterY());

				if (rect.contains(center1)) {
					secondRect = findRect(contours, center2);
					break;
				} else if (rect.contains(center2)) {
					secondRect = findRect(contours, center1);
					break;
				}

			}
			if (secondRect != null) {
				Rectangle first = new Rectangle(rect.x, rect.y, rect.width,
						rect.height);
				Rectangle second = new Rectangle(secondRect.x, secondRect.y,
						secondRect.width, secondRect.height);

				MatchPair match = new MatchPair(first, second);

				list.add(match);
			}
		}

		return list;
	}

	public double[] getZernikeFeatures(BufferedImage img,
			List<Rectangle> areas, Config config) {

		double[] results = new double[0];
		int i = 0;

		// calculate features for each region of interest
		List<BufferedImage> subimages = new ArrayList<BufferedImage>();
		int numOfPixels = 0;
		while (i < areas.size()) {
			Rectangle a = areas.get(i);
			if (a.width < config.block_size || a.height < config.block_size) {
				i++;
				continue;
			}
			subimages.add(img.getSubimage(a.x, a.y, a.width, a.height));
			numOfPixels += a.width * a.height;

			i++;
		}
		ProgressMonitor monitor = null;
		if (!config.useCUDA) {
			monitor = new ProgressMonitor(String.format("Computing subimage %d of %d", 1,
							subimages.size()), 0, numOfPixels);
		}
		int progress = 0;
		int sub_i = 2;
		for (BufferedImage sub : subimages) {
			double[] cuda_results;
			if (config.useCUDA) {
				cuda_results = getZernikeCUDAResults(sub, config);
			} else {
				cuda_results = getZernikeCPUResults(sub, config, monitor,
						progress);
			}
			progress += sub.getHeight() * sub.getWidth();

			// remove complete blocks below threshold from results and
			// adapt the x,y values for the results
			Rectangle a = areas.get(sub_i - 2);
			int offset = removeAllUnvalids(cuda_results, a);
			log.info(String.format("%d entries removed", -offset / 14));
			if (!config.useCUDA)
				monitor.setNote(String.format("Computing subimage %d of %d",
						sub_i, subimages.size()));
			results = appendToResults(results, cuda_results, offset);
			sub_i++;
		}
		if (!config.useCUDA)
			monitor.setProgress(monitor.getMaximum());

		return results;
	}

	public double[] appendToResults(double[] results, double[] new_results,
			int offset) {
		int old_pos = results.length;
		results = Arrays.copyOf(results, results.length
				+ new_results.length + offset);
		System.arraycopy(new_results, 0, results, old_pos,
				new_results.length + offset);
		return results;
	}

	public int removeAllUnvalids(double[] cuda_results, Rectangle image) {
		int offset = 0;
		for (int d = 12; d < cuda_results.length; d += 14) {
			if (cuda_results[d] == -1 || cuda_results[d + 1] == -1) {
				offset -= 14;
				continue;
			}
			for (int m = 12; m > 0; m--) {
				cuda_results[d - m + offset] = cuda_results[d - m];
			}
			cuda_results[d + offset] = cuda_results[d] + image.x;
			cuda_results[d + 1 + offset] = cuda_results[d + 1] + image.y;
		}
		return offset;
	}

	public double[] getZernikeCPUResults(BufferedImage testimage, Config config) {
		return getZernikeCPUResults(testimage, config, null, -1);
	}

	private double[] getZernikeCPUResults(BufferedImage gray_img,
			Config config, ProgressMonitor monitor, int old_progress) {

		int height = gray_img.getHeight();
		int width = gray_img.getWidth();
		int feature_size = 12;
		// initialize results matrix
		int linenumber_results = ((height - config.block_size + 1) * (width
				- config.block_size + 1));

		int[] data = new int[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
			
				data[x * height + y] = gray_img.getRGB(x, y) & 0xFF;
			}
		}


		double[] output = new double[(feature_size + 2) * linenumber_results];

		ExecutorService service = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());

		List<Future<?>> tasks = new ArrayList<Future<?>>();

		for (int x = 0; x < gray_img.getWidth(); x++) {
			for (int y = 0; y < gray_img.getHeight(); y++) {
				ZernikeCPU task = new ZernikeCPU(config.block_size, width,
						height, data, config.range_threshold,
						config.avg_intensity_threshold, output, x, y);
				tasks.add(service.submit(task));
			}
		}

		service.shutdown();

		try {
			while (!service.isTerminated()) {
				int numDone = 0;
				for (Future<?> f : tasks) {
					numDone += f.isDone() ? 1 : 0;
				}
				if (monitor != null)
					monitor.setProgress(old_progress + numDone);

				Thread.sleep(3000);
			}
		} catch (InterruptedException e) {
			log.throwing(e);
		}

		return output;

	}

	public double[] getZernikeCUDAResults(BufferedImage gray_img, Config config) {
		int height = gray_img.getHeight();
		int width = gray_img.getWidth();
		int feature_size = 12;

		// initialize results matrix
		int linenumber_results = ((height - config.block_size + 1) * (width
				- config.block_size + 1));
		// load the kernel

		cuInit(0);
		CUdevice device = new CUdevice();
		cuDeviceGet(device, 0);
		CUcontext context = new CUcontext();
		cuCtxCreate(context, 0, device);

		// Load the PTX that contains the kernel.
		CUmodule module = new CUmodule();
		int status = cuModuleLoad(module, "Release/zernikepolynomial.ptx");

		// Obtain a handle to the kernel function.
		CUfunction function = new CUfunction();
		status = cuModuleGetFunction(function, module,
				"_Z27computeZernikeFeatureVectoriiiPhiiPd");


		// Allocate the device input data, and copy the
		// host input data to the device
		CUdeviceptr deviceInput = new CUdeviceptr();
		CUdeviceptr deviceOutput = new CUdeviceptr();
		int[] data = new int[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[x * height + y] = gray_img.getRaster().getSample(x, y, 0);
			}
		}
		byte[] byte_data = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			byte_data[i] = (byte) data[i];
		}

		cuMemAlloc(deviceInput, byte_data.length * Sizeof.BYTE);
		cuMemcpyHtoD(deviceInput, Pointer.to(byte_data), byte_data.length
				* Sizeof.BYTE);
		cuMemAlloc(deviceOutput, (feature_size + 2) * linenumber_results
				* Sizeof.DOUBLE);

		// Set up the kernel parameters
		Pointer kernelParameters = Pointer.to(
				Pointer.to(new int[] { config.block_size }),
				Pointer.to(new int[] { width }),
				Pointer.to(new int[] { height }), Pointer.to(deviceInput),
				Pointer.to(new int[] { config.range_threshold }),
				Pointer.to(new int[] { config.avg_intensity_threshold }),
				Pointer.to(deviceOutput));

		// Call the kernel function.
		status = cuLaunchKernel(function, (int) Math.ceil((double) (width) / config.block_size),
				(int) Math.ceil((double) (height) / config.block_size), 1, // Grid dimension
				config.block_size, config.block_size, 1, // Block dimension
				0, null, // Shared memory size and stream
				kernelParameters, null // Kernel- and extra parameters
		);


		// Copy the data back from the device to the host and clean up
		double[] output = new double[(feature_size + 2) * linenumber_results];
		cuMemcpyDtoH(Pointer.to(output), deviceOutput, output.length
				* Sizeof.DOUBLE);
		cuMemFree(deviceInput);
		cuMemFree(deviceOutput);

		return output;
	}


	public Set<MatchPair> matchFeatures(double[] results, Config config) {
		Set<MatchPair> mark_blocks = new HashSet<MatchPair>();
		Flann.INSTANCE.flann_set_distance_type(
				FlannDistanceType.FLANN_DIST_EUCLIDEAN, -1);
		Flann.FLANNParameters build_params = new Flann.FLANNParameters();
		build_params.algorithm = FlannAlgorithmType.FLANN_INDEX_KDTREE;
		build_params.checks = 32;
		build_params.eps = 0.0f;
		build_params.sorted = 1;
		build_params.max_neighbors = -1;
		build_params.cores = 2;
		build_params.trees = 25;
		build_params.branching = 32;
		build_params.iterations = 5;
		build_params.centers_init = FlannCentersInitType.FLANN_CENTERS_RANDOM;
		build_params.cb_index = 0.4f;
		build_params.target_precision = 1.0f;
		build_params.build_weight = 0.01f;
		build_params.memory_weight = 0.01f;
		build_params.sample_fraction = 0.1f;
		build_params.table_number_ = 12;
		build_params.key_size_ = 20;
		build_params.multi_probe_level_ = 2;
		build_params.log_level = FlannLogLevelType.FLANN_LOG_WARN;
		build_params.random_seed = 0;

		double[] filtered_results = extractFeatures(results);

		int result_size = results.length / 14;

		int knn = config.knn;
		int[] indices = new int[result_size * knn];
		double[] dists = new double[result_size * knn];
		Flann.INSTANCE.flann_find_nearest_neighbors_double(filtered_results,
				result_size, 12, filtered_results, result_size, indices, dists,
				knn, build_params);

		for (int i = 0; i < result_size; i++) {
			if (i % 10000 == 0)
				System.out.println(String.format("%d / %d", i, result_size));

			for (int j = 0; j < knn; j++) {
				int index_k = indices[i * knn + j];
				double dist = dists[i * knn + j];
				if (index_k == i)
					continue;

				if (dist > config.nn_distance)
					break;

				// actual block
				double x1 = results[i * 14 + 12];
				double y1 = results[i * 14 + 13];
				// matching block
				double x2 = results[index_k * 14 + 12];
				double y2 = results[index_k * 14 + 13];
				// Check euclidean distance between these two blocks
				if (Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) > 25) {
					mark_blocks.add(new MatchPair(new Rectangle((int) (x1 + 1),
							(int) (y1 + 1), 16, 16), new Rectangle(
							(int) (x2 + 1), (int) (y2 + 1), 16, 16)));
				}

			}
		}
		

		return mark_blocks;
	}

	public double[] extractFeatures(double[] results) {
		double[] filtered_results = new double[results.length / 14 * 12];

		for (int i = 0; i < results.length; i++) {
			int d = i % 14;
			if (d >= 12)
				continue;
			filtered_results[i / 14 * 12 + d] = results[i];
		}
		return filtered_results;
	}

	public Set<MatchPair> filterMatches(Set<MatchPair> pairs) {
		Set<MatchPair> mark_blocks = new HashSet<>();
		for (Iterator<MatchPair> it = pairs.iterator(); it.hasNext();) {
			MatchPair t = it.next();
			int num_near = 0;
			double dist_sum = 0;
			int xMax = Integer.MIN_VALUE;
			int xMin = Integer.MAX_VALUE;
			int yMax = Integer.MIN_VALUE;
			int yMin = Integer.MAX_VALUE;

			for (MatchPair p : pairs) {
				if (t.dist(p) < 60) {
					num_near++;
					dist_sum += t.dist(p);
					xMax = Math.max(p.getMaxX(), xMax);
					xMin = Math.min(p.getMinX(), xMin);
					yMax = Math.max(p.getMaxY(), yMax);
					yMin = Math.min(p.getMinY(), yMin);

				}
			}
			int area = (xMax - xMin) * (yMax - yMin);
			if (num_near > 70 && (dist_sum / num_near) > 20
					&& (dist_sum / num_near) < 50) {
				System.out.println(area + " " + num_near + " "
						+ (dist_sum / num_near));
				// mark_blocks.add(new Rectangle(xMin, yMin, xMax - xMin + 16,
				// yMax - yMin + 16));
				mark_blocks.add(t);
			} else {
				it.remove();
			}

		}

		return mark_blocks;
	}


	public Set<Rectangle> markBlocks(Set<MatchPair> matches) {
		Set<Rectangle> mark_blocks = new HashSet<>();
		for (MatchPair p : matches) {
			mark_blocks.add(p.getFirst());
			mark_blocks.add(p.getSecond());
		}
		return mark_blocks;
	}

	private static Rect findRect(List<MatOfPoint> rects, Point point) {
		for (MatOfPoint mop : rects) {
			Rect rect = Imgproc.boundingRect(mop);
			if (rect.contains(point))
				return rect;
		}
		return null;
	}
}
