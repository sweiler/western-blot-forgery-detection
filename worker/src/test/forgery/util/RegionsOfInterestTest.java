package test.forgery.util;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import forgery.util.Config;
import forgery.util.Utility;

public class RegionsOfInterestTest {
	
	private static String input_filename = "test_images/base_attacks_rotate_5_scale_105.png";
	
	public static void main(String[] args) {
		Utility util = Utility.instance();
		
		try {
			BufferedImage img = util.convertToGreyscale(util
					.imread(input_filename));
			
			Config config = util.getConfiguration();
			
			BufferedImage n = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = (Graphics2D) n.getGraphics();
			
			g.setStroke(new BasicStroke(1));
			Raster r = img.getData();
			for(int y = 0; y < img.getHeight(); y++) {
				for(int x = 0; x < img.getWidth(); x++) {
					byte k = (byte) (r.getSample(x, y, 0) & 0xFF);
					int v = k & 0xFF;
					int rgb = v << 16 | v << 8 | v;
					g.setColor(new Color(rgb));
					g.fillRect(x, y, 1, 1);
				}
			}
			
			ImageIO.write(n, "PNG", new FileOutputStream("outputEnde.png"));
			/*
			
			List<Rectangle> areas = util.getRegionsOfInterest(img, config);
			
			BufferedImage n = new BufferedImage(img.getWidth(),
					img.getHeight(), BufferedImage.TYPE_INT_RGB);

			Graphics2D g = (Graphics2D) n.getGraphics();
			g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);

			g.setColor(Color.red);
			g.setStroke(new BasicStroke(1));
			for (Rectangle r : areas) {
				g.drawRect(r.x, r.y, r.width, r.height);
			}
			
			ImageIO.write(n, "PNG", new FileOutputStream("outputEnde.png"));*/
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
	}
}
