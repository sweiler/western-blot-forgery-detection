package test.forgery.util;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import forgery.util.MatchPair;

public class ClusteringTest {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		List<MatOfPoint> contours = new ArrayList<>();
		HashSet<MatchPair> matches;

		try {
			FileInputStream fis = new FileInputStream(new File("matches.dat"));
			ObjectInputStream ois = new ObjectInputStream(fis);

			matches = (HashSet<MatchPair>) ois.readObject();

		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		Mat binary = Highgui.imread("test.png", CvType.CV_8UC1);

		Imgproc.findContours(binary, contours, new Mat(),
				Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		HashSet<MatchPair> clusteredMatches = new HashSet<>();

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

				clusteredMatches.add(match);
			}
		}

		Mat result = binary.clone();
		for (MatchPair p : clusteredMatches) {
			Core.line(result, new Point(p.getFirst().getCenterX(), p.getFirst()
					.getCenterY()), new Point(p.getSecond().getCenterX(), p
					.getSecond().getCenterY()), new Scalar(0xFFFFFF));
		}
		
		Highgui.imwrite("clusters.png", result);
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
