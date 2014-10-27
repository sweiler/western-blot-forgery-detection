package forgery.web.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import forgery.web.ReportPairController;

public class LocalContrastTest {
	public static void main(String[] args) {
		ReportPairController uut = new ReportPairController();
		try {
			BufferedImage input = ImageIO.read(new File("/home/simon/snow.jpg"));
			uut.increaseLocalContrast(input);
			ImageIO.write(input, "PNG", new File("/home/simon/outSnow.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
