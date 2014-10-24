package forgery.util;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
	private Config conf;

	private Utility() {

	}

	public Config getConfiguration() {
		if (conf == null) {
			try {
				JAXBContext ctx = JAXBContext.newInstance(Config.class);
				Unmarshaller unmarshaller = ctx.createUnmarshaller();
				conf = (Config) unmarshaller.unmarshal(configFile);
				log.info("Config-File successfully read");
			} catch (JAXBException e) {
				log.error("The Config-File has some error!");
				throw new Error("Error on parsing Config-File", e);
			}
		}
		return conf;
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

		Mat holes = loaded.clone();

		Scalar fillColor = new Scalar(255, 255, 255, 1);

		Mat mask = new Mat();

		Imgproc.floodFill(holes, mask, new Point(0, 0), fillColor);

		Core.bitwise_not(holes, holes);

		Mat white = loaded.clone();

		int width = loaded.width();
		int height = loaded.height();

		Core.rectangle(white, new Point(0, 0), new Point(width, height),
				fillColor, Core.FILLED);

		white.copyTo(loaded, holes);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

		Imgproc.findContours(loaded, contours, new Mat(),
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		for (MatOfPoint mop : contours) {
			Rect rect = Imgproc.boundingRect(mop);

			int w = rect.width - 8;
			int h = rect.height - 8;
			int x = rect.x + 4;
			int y = rect.y + 4;

			if (w < config.threshold_width * width
					|| h < config.threshold_height * height
					|| (w * h) < config.threshold_sum * height * width)
				continue;

			Rectangle r = new Rectangle(x, y, w, h);
			boolean intersects = false;

			// Remove intersections
			for (Rectangle o : regions) {
				if (o.intersects(r)) {
					intersects = true;
					break;
				}
			}

			if (!intersects)
				regions.add(r);

		}

		if (regions.size() == 0) {
			log.warn("No areas detected, using complete image");
			regions.add(new Rectangle(0, 0, width, height));
		}

		log.info("Regions of interest successfully computed");

		return regions;
	}

}
