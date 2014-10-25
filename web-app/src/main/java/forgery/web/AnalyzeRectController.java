package forgery.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.ImageRect;
import forgery.web.model.FileState;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

public class AnalyzeRectController extends HttpServlet {
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
		User user = (User) request.getSession().getAttribute("user");

		if (user == null) {
			response.setStatus(403);
			response.getWriter().write("User not authenticated\n");
			response.getWriter().close();
			return;
		}

		String imgIdParam = request.getPathInfo().replace("/", "");
		int imgId = -1;
		try {
			imgId = Integer.parseInt(imgIdParam);
		} catch (NumberFormatException e) {
			response.setStatus(400);
			response.getWriter().write("Image-ID required\n");
			response.getWriter().close();
			return;
		}

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		UploadedFile file = (UploadedFile) session.byId(UploadedFile.class)
				.load(imgId);

		@SuppressWarnings("unchecked")
		List<ImageRect> rects = session.createCriteria(ImageRect.class)
				.createCriteria("file").add(Restrictions.idEq(imgId))
				.list();

		session.getTransaction().commit();
		session.close();

		if (file.getUser().getId() != user.getId()) {
			response.setStatus(403);
			response.getWriter().write("You are not owner of this image.\n");
			response.getWriter().close();
			return;
		}

		FileStorageService fs = FileStorageService.instance();
		BufferedImage imgData = ImageIO.read(new ByteArrayInputStream(fs
				.loadData(file.getHash())));

		Writer w = response.getWriter();
		w.write("{ \"imgWidth\" : \"" + imgData.getWidth());
		w.write("\", \"imgHeight\" : \"" + imgData.getHeight());
		w.write("\",\n \"data\" : \"");
		for (ImageRect r : rects) {

			w.write("<div class=\\\"rect\\\" style=\\\"left: " + r.getX()
					+ "px; top: " + r.getY() + "px; width: " + r.getWidth()
					+ "px; height: " + r.getHeight() + "px;\\\">" + "</div>");

		}
		w.write("\" }");
		w.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");

		if (user == null) {
			response.setStatus(403);
			response.getWriter().write("User not authenticated\n");
			response.getWriter().close();
			return;
		}

		String imgIdParam = request.getPathInfo().replace("/", "");
		int imgId = -1;
		try {
			imgId = Integer.parseInt(imgIdParam);
		} catch (NumberFormatException e) {
			response.setStatus(400);
			response.getWriter().write("Image-ID required\n");
			response.getWriter().close();
			return;
		}

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		UploadedFile f = (UploadedFile) session.byId(UploadedFile.class).load(
				imgId);
		if (f == null) {
			response.setStatus(404);
			response.getWriter().write("Image not found\n");
			response.getWriter().close();
		} else if (f.getUser().getId() != user.getId()) {
			response.setStatus(403);
			response.getWriter().write("You are not owner of this image.\n");
			response.getWriter().close();
		} else {
			for (ImageRect r : (List<ImageRect>) session
					.createCriteria(ImageRect.class)
					.createCriteria("file")
					.add(Restrictions.idEq(imgId))
					.list()) {
				session.delete(r);
			}

			int num = Integer.parseInt(request.getParameter("num"));
			for (int i = 0; i < num; i++) {
				ImageRect r = new ImageRect();
				r.setFile(f);

				String t = request.getParameter("rect" + i + "_t");

				double tD = Double.parseDouble(t);
				r.setY((int) tD);

				String l = request.getParameter("rect" + i + "_l");

				double lD = Double.parseDouble(l);
				r.setX((int) lD);

				String w = request.getParameter("rect" + i + "_w");

				double wD = Double.parseDouble(w);
				r.setWidth((int) wD);

				String h = request.getParameter("rect" + i + "_h");

				double hD = Double.parseDouble(h);
				r.setHeight((int) hD);

				session.save(r);
			}
		}
		f.setState(FileState.START_ANALYZE);
		session.update(f);
		session.getTransaction().commit();
		session.close();
		response.getWriter().close();
		TaskController.informTasks();
	}

}
