package forgery.web;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.ReportPair;

public class ReportPairController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static SessionFactory sessionFactory;
	private static final int rectWidth = 120;

	@Override
	public void destroy() {
		sessionFactory.close();
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		Configuration config = new Configuration();
		config.configure();
		ServiceRegistry reg = (new StandardServiceRegistryBuilder())
				.applySettings(config.getProperties()).build();
		sessionFactory = (new Configuration()).configure().buildSessionFactory(
				reg);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();

		String[] parts = request.getPathInfo().split("/");

		if (parts.length < 3 || parts[1] == null || parts[2] == null) {
			response.setStatus(400);
			response.getWriter().write("Report-Pair-ID required\n");
			response.getWriter().close();
			return;
		}
		String reportId = parts[1];
		try {
			int reportPairId = Integer.parseInt(parts[2]);

			Session session = sessionFactory.openSession();
			session.beginTransaction();

			ReportPair pair = (ReportPair) session.byId(ReportPair.class).load(
					reportPairId);

			if (pair == null) {
				session.close();
				response.setStatus(404);
				return;
			}
			
			if(!pair.getReport().getId().equals(reportId)) {
				session.close();
				response.setStatus(401);
				return;
			}
			FileStorageService fs = FileStorageService.instance();
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(fs
					.loadData(pair.getReport().getFile().getHash())));

			BufferedImage output = new BufferedImage(rectWidth * 3, rectWidth,
					BufferedImage.TYPE_INT_RGB);

			Rectangle r1 = pair.getFirst();
			Rectangle r2 = pair.getSecond();
			
			Rectangle[] rects1 = transformRect(r1);
			Rectangle[] rects2 = transformRect(r2);
			
			Rectangle drawR1 = rects1[0];
			Rectangle drawR2 = rects2[0];
			
			Graphics2D outputGraphics = output.createGraphics();

			drawRectangle(img, drawR1, outputGraphics, 0);
			drawRectangle(img, drawR2, outputGraphics, 1);
			
			for(int x = 0; x < rectWidth; x++) {
				for(int y = 0; y < rectWidth; y++) {
					int rgb1 = output.getRGB(x, y);
					int rgb2 = output.getRGB(x + rectWidth, y);
					int greyscale1 = toGreyscale(rgb1);
					int greyscale2 = toGreyscale(rgb2);
					
					int diff = Math.abs(greyscale1 - greyscale2);
					output.setRGB(x + rectWidth * 2, y, diff << 16 | diff << 8 | diff);
				}
			}
			
			drawRedRect(outputGraphics, rects1[1], 0);
			drawRedRect(outputGraphics, rects2[1], 1);
			

			ImageIO.write(output, "PNG", response.getOutputStream());
			response.getOutputStream().close();

		} catch (NumberFormatException e) {
			response.setStatus(400);
			response.getWriter().write("Report-Pair-ID is not an integer\n");
			response.getWriter().close();
			return;
		}

	}
	
	private void drawRedRect(Graphics2D outputGraphics, Rectangle red, int xMod) {
		outputGraphics.setColor(new Color(0x40FF0000, true));
		outputGraphics.fillRect(red.x + xMod * rectWidth, red.y, red.width, red.height);
	}

	private int toGreyscale(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return (int) (0.2989 * r + 0.5870 * g + 0.1140 * b);
	}

	private Rectangle[] transformRect(Rectangle input) {
		if(input.height <= rectWidth && input.width <= rectWidth) {
			int xdiff = (rectWidth - input.width) / 2;
			int ydiff = (rectWidth - input.height) / 2;
			Rectangle r1 = new Rectangle(input.x - xdiff, input.y - ydiff, rectWidth, rectWidth);
			Rectangle r2 = new Rectangle(xdiff, ydiff, input.width, input.height);
			return new Rectangle[] {r1, r2};
		} else if(input.height < input.width) {
			double fac = ((double) input.width) / rectWidth;
			int newHeight = (int) (input.height / fac);
			int diff = (rectWidth - newHeight) / 2;
			Rectangle r1 = new Rectangle(input.x, input.y - diff, input.width, input.width);
			Rectangle r2 = new Rectangle(0, diff, rectWidth, newHeight);
			return new Rectangle[] {r1, r2};
		} else {
			double fac = ((double) input.height) / rectWidth;
			int newWidth = (int) (input.width / fac);
			int diff = (rectWidth - newWidth) / 2;
			Rectangle r1 = new Rectangle(input.x - diff, input.y, input.height, input.height);
			Rectangle r2 = new Rectangle(diff, 0, newWidth, rectWidth);
			return new Rectangle[] {r1, r2};
		}
	}

	private void drawRectangle(BufferedImage img, Rectangle drawR1, Graphics2D outputGraphics, int xMod) {
		
		outputGraphics.drawImage(img, xMod * rectWidth, 0, (1 + xMod) * rectWidth, rectWidth,
				drawR1.x, drawR1.y, drawR1.x + drawR1.width, drawR1.y + drawR1.height, new NullObserver());
		
	}
	
	private class NullObserver implements ImageObserver {

		@Override
		public boolean imageUpdate(Image img, int infoflags,
				int x, int y, int width, int height) {
			return true;
		}
	}
}
