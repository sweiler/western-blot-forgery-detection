package forgery.web;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.FileType;
import forgery.web.model.Report;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

/**
 * Servlet implementation class ThumbnailController
 */
public class ImageController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SessionFactory sessionFactory;
	private static final int thumbWidth = 244, thumbHeight = 110;

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

	@Override
	public void destroy() {
		sessionFactory.close();
	}

	private BufferedImage generateThumbImage(byte[] inputData)
			throws IOException {
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(inputData));
		BufferedImage thumb = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		thumb.getGraphics().setColor(Color.white);
		thumb.getGraphics().fillRect(0, 0, thumbWidth, thumbHeight);
		double ratio = ((double) img.getWidth()) / img.getHeight();
		double thisRatio = ((double) thumbWidth) / thumbHeight;
		if (thisRatio < ratio) {
			thumb.getGraphics().drawImage(img,
					(int) (-(thumbHeight * ratio - thumbWidth) / 2), 0,
					(int) (thumbHeight * ratio), thumbHeight, null);
		} else {
			thumb.getGraphics().drawImage(img, 0,
					(int) (-(thumbWidth / ratio - thumbHeight) / 2),
					thumbWidth, (int) (thumbWidth / ratio), null);
		}

		return thumb;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();
		FileStorageService fs = FileStorageService.instance();
		boolean thumbRequested = request.getServletPath().equals("/Thumbnails");
		if (request.getPathInfo().replace("/", "").equals("dummy")) {
			IOUtils.copy(context.getResourceAsStream("images/dummy.png"),
					response.getOutputStream());
			response.getOutputStream().close();
			return;
		}

		User user = (User) request.getSession().getAttribute("user");
		response.setContentType("image/png");

		int id = Integer.parseInt(request.getPathInfo().replace("/", ""));

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		UploadedFile file = (UploadedFile) session.byId(UploadedFile.class)
				.load(id);
		if (file == null) {
			response.setStatus(404);
			IOUtils.copy(context.getResourceAsStream("images/dummy.png"),
					response.getOutputStream());
			response.getOutputStream().close();
			return;
		}

		if ((user == null || user.getId() != file.getUser().getId())) {
			response.setStatus(403);
			IOUtils.copy(context.getResourceAsStream("images/dummy.png"),
					response.getOutputStream());
			response.getOutputStream().close();
		} else if (!thumbRequested) {
			ByteArrayInputStream bis = new ByteArrayInputStream(
					fs.loadData(file.getHash()));

			IOUtils.copy(bis, response.getOutputStream());
			response.getOutputStream().close();
		} else if (fs.loadThumb(file.getHash()) == null) {
			BufferedImage generatedThumb = null;
			if (file.getType() == FileType.PNG
					|| file.getType() == FileType.JPEG) {
				generatedThumb = generateThumbImage(fs.loadData(file.getHash()));
			} else if(file.getType() == FileType.PDF) {
				try {
					Process proc = fs.convertProcessPDFtoPNG(file.getHash());
					proc.waitFor();
					IOUtils.copy(proc.getErrorStream(), System.err);
					IOUtils.copy(proc.getInputStream(), System.out);
					generatedThumb = generateThumbImage(fs.loadGeneratedPNG(file.getHash()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(generatedThumb, "PNG", bos);
			fs.storeThumb(file.getHash(), bos.toByteArray());
			session.update(file);
			ImageIO.write(generatedThumb, "PNG", response.getOutputStream());
			response.getOutputStream().close();
		} else {

			ByteArrayInputStream bis = new ByteArrayInputStream(
					fs.loadThumb(file.getHash()));

			IOUtils.copy(bis, response.getOutputStream());
			response.getOutputStream().close();
		}
		session.getTransaction().commit();
		session.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
