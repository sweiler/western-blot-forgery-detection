package forgery.web;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import forgery.util.Config;
import forgery.util.Utility;
import forgery.web.model.ImageRect;
import forgery.web.model.FileState;
import forgery.web.model.FileType;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

@MultipartConfig
public class FileUploadController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static SessionFactory sessionFactory;
	private Logger log = LogManager.getLogger(this.getClass().getSimpleName());

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

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.info("Recieved GET message, but this service endpoint only accepts POST requests");
		response.getWriter().write("Test\n");
		response.getWriter().close();
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();

		FileStorageService fs = FileStorageService.instance();

		User user;
		if (request.getSession().getAttribute("user") == null) {
			user = new User();
			user.generateAuthToken();
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			session.save(user);
			session.getTransaction().commit();
			session.close();
			request.getSession().setAttribute("user", user);
			log.debug("Created anonymous user for file upload");
		} else {
			user = (User) request.getSession().getAttribute("user");
		}

		Part filepart = request.getPart("file");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copy(filepart.getInputStream(), bos);

		String filename = request.getParameter("filename");
		FileType fileType = null;
		if (filename.toLowerCase().endsWith(".png")) {
			fileType = FileType.PNG;
		} else if (filename.toLowerCase().endsWith(".jpg")
				|| filename.toLowerCase().endsWith(".jpeg")) {
			fileType = FileType.JPEG;
		} else if (filename.toLowerCase().endsWith(".pdf")) {
			fileType = FileType.PDF;
		} else if (filename.toLowerCase().endsWith(".zip")) {
			fileType = FileType.ZIP;
		}

		if (fileType == null) {
			response.setStatus(415);
			return;
		}
		List<Rectangle> rects = null;
		if (fileType == FileType.JPEG || fileType == FileType.PNG) {
			// Compute regions of interest
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bos
					.toByteArray()));
			Utility util = Utility.instance();
			Config conf = util.getConfiguration();
			log.debug("Computing regions of interest");
			rects = util.getRegionsOfInterest(util.convertToGreyscale(img),
					conf);
		}

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		UploadedFile newFile = new UploadedFile();

		fs.storeData(bos.toByteArray());
		newFile.setBinaryData(bos.toByteArray());
		newFile.setCreated(new GregorianCalendar());
		newFile.setState(FileState.NEWLY_CREATED);
		newFile.setType(fileType);
		newFile.setUser(user);
		newFile.setFilename(request.getParameter("filename"));

		int fileId = (Integer) session.save(newFile);
		if (rects != null) {
			for (Rectangle rect : rects) {
				ImageRect r = new ImageRect();
				r.setFile(newFile);
				r.setX(rect.x);
				r.setY(rect.y);
				r.setWidth(rect.width);
				r.setHeight(rect.height);
				session.save(r);
				newFile.getRects().add(r);
			}
		}
		user = (User) session.merge(user);
		user.getFiles().add(newFile);

		session.getTransaction().commit();
		session.close();
		log.debug("New uploaded file created for user " + user.getId());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(
				"{\"status\": \"ok\", \"id\": " + fileId + ", \"type\": \"" + newFile.getType().name() + "\"}");
		response.getWriter().close();

		if (fileType == FileType.PDF) {
			Thread thread = new Thread(new ExtractPdf(newFile.getId(),
					newFile.getHash()));
			thread.start();
		}
	}

	private class ExtractPdf implements Runnable {
		private int id;
		private String hash;

		public ExtractPdf(int id, String hash) {
			this.id = id;
			this.hash = hash;
		}

		@Override
		public void run() {

			FileStorageService fs = FileStorageService.instance();
			try {
				Process extractImagesProcess = fs.extractImagesProcess(hash);

				int code = extractImagesProcess.waitFor();
				if (code != 0)
					throw new IllegalStateException("extract code: " + code);

				Process convertExtractedImages = fs.convertExtracted(hash);
				code = convertExtractedImages.waitFor();
				if (code != 0)
					throw new IllegalStateException("convert code: " + code);
				Session session = sessionFactory.openSession();
				session.beginTransaction();

				UploadedFile parent = (UploadedFile) session.byId(
						UploadedFile.class).load(id);
				File folder = new File(fs.filePathExtracted(hash));
				int i = 0;
				for (File child : folder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".png");
					}
				})) {
					FileInputStream fis = new FileInputStream(child);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					IOUtils.copy(fis, bos);
					String newHash = fs.storeData(bos.toByteArray());
					UploadedFile newFile = new UploadedFile();
					newFile.setHash(newHash);
					newFile.setCreated(new GregorianCalendar());
					newFile.setParent(parent);
					newFile.setUser(parent.getUser());
					newFile.setState(FileState.NEWLY_CREATED);
					newFile.setStateChange(new GregorianCalendar());
					newFile.setType(FileType.PNG);
					newFile.setFilename(i + " - " + parent.getFilename());
					session.persist(newFile);
					i++;
				}
				

				parent.setState(FileState.EXTRACTED);
				session.update(parent);

				session.getTransaction().commit();
				session.close();

			} catch (Exception e) {
				e.printStackTrace();
				Session session = sessionFactory.openSession();
				session.beginTransaction();

				UploadedFile file = (UploadedFile) session.byId(
						UploadedFile.class).load(id);
				file.setState(FileState.UNKNOWN_ERROR);
				session.update(file);

				session.getTransaction().commit();
				session.close();
			}
		}

	}
}
