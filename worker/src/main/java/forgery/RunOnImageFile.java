package forgery;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import forgery.util.Config;
import forgery.util.MatchPair;
import forgery.util.Utility;

public class RunOnImageFile {

	private static String input_filename = "test_images/base_attacks_rotate_5_scale_105.png";
	private static String output_filename = "output2.png";

	public static void main(String[] args) {
		System.setProperty("log4j.configurationFile", "log4j2.xml");

		Logger log = LogManager.getLogger("Detector");
		Utility util = Utility.instance();

		try {
			Config config = util.getConfiguration();

			BufferedImage img = util.convertToGreyscale(util
					.imread(input_filename));

			List<Rectangle> areas = util.getRegionsOfInterest(img, config);
			double[] results;

			// First pass: focus on background copies
			config.nn_distance = 50;
			// very low range threshold: only complete monotonous blocks are
			// excluded
			config.range_threshold = 0;
			config.avg_intensity_threshold = 0;
			// Feature Extraction
			results = util.getZernikeFeatures(img, areas, config);
			log.info(String.format(
					"(PASS 1) Features for all %d subimages computed",
					areas.size()));

			BufferedWriter bw = new BufferedWriter(new FileWriter("data.txt"));
			for (int i = 0; i < results.length; i++) {
				bw.append(Double.toString(results[i]));
				if (i % 14 < 13)
					bw.append(",");
				else
					bw.append("\n");
			}
			bw.flush();
			bw.close();

			Set<MatchPair> mark_blocks = util.matchFeatures(results, config);
			/*
			FileReader fr = new FileReader("mark_blocks2.txt");
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			Set<MatchPair> mark_blocks = new HashSet<MatchPair>();
			while (line != null) {
				Rectangle first = new Rectangle();
				Rectangle second = new Rectangle();
				processLine(line, first);
				line = br.readLine();
				if(line != null) {
					processLine(line, second);
					mark_blocks.add(new MatchPair(first, second));
					line = br.readLine();
				}
			}
			*/
			

			// matches = util.filterMatches(matches);

			// Set<MatchPair> mark_blocks = util.filterMatches(matches);

			log.info("(PASS 1) Matching completed");
			/*
			 * // Second pass: focus on blot copies config.nn_distance = 800;
			 * config.range_threshold = 30; // Feature Extraction results =
			 * util.getZernikeFeatures(img, areas, config);
			 * log.info(String.format(
			 * "(PASS 2) Features for all %d subimages computed",
			 * areas.size()));
			 * 
			 * // Feature Matching matches = util.matchFeatures(results,
			 * config); mark_blocks.addAll(util.filterMatches(matches));
			 * log.info("(PASS 2) Matching completed");
			 */

			// List<MatchPair> clusters =
			// util.clusteredMatches(img.getWidth(), img.getHeight(),
			// mark_blocks);

			BufferedImage n = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_RGB);

			Graphics2D g = (Graphics2D) n.getGraphics();
			g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);

			g.setColor(Color.red);
			g.setStroke(new BasicStroke(1));
			for (Rectangle r : areas) {
				g.drawRect(r.x, r.y, r.width, r.height);
			}

			g.setColor(new Color(0x32FF0000, true));

			for (MatchPair p : mark_blocks) {
				Rectangle r = p.getFirst();
				g.fillRect(r.x, r.y, r.width, r.height);
				r = p.getSecond();
				g.fillRect(r.x, r.y, r.width, r.height);
			}

			ImageIO.write(n, "PNG", new File(output_filename));

			log.info("Finished");
		} catch (IOException e) {
			log.catching(e);
		}

	}

	private static void processLine(String line, Rectangle rect) {
		String[] parts = line.split(",");
		int x = Integer.parseInt(parts[0]);
		int y = Integer.parseInt(parts[1]);
		int w = 16;
		rect.x = x;
		rect.y = y;
		rect.height = w;
		rect.width = w;
	}


}
