package forgery.web;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.ImageRect;
import forgery.web.model.Report;
import forgery.web.model.ReportPair;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

public class ReportController extends HttpServlet {

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

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();

		String[] parts = request.getPathInfo().split("/");

		
		if(parts.length < 2 || parts[1] == null) {
			response.setStatus(400);
			response.getWriter().write("Report-ID required\n");
			response.getWriter().close();
			return;
		}
		String reportId = parts[1];
		

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Report report = (Report) session.byId(Report.class).load(reportId);
		if(report == null) {
			session.getTransaction().commit();
			session.close();
			response.setStatus(400);
			response.getWriter().write("Report-ID not found!\n");
			response.getWriter().close();
			return;
		}
		@SuppressWarnings("unchecked")
		List<ReportPair> pairs = session.createCriteria(ReportPair.class)
				.createCriteria("report").add(Restrictions.idEq(reportId))
				.list();

		UploadedFile file = report.getFile();
		if(file != null)
			file.getHash();

		
		session.getTransaction().commit();
		session.close();

		
		
		if(file == null) {
			response.setStatus(404);
			response.getWriter().write("No corresponding file for this report found.\n");
			response.getWriter().close();
			return;
		}
		
		request.setAttribute("report", report);
		request.setAttribute("file", file);
		request.setAttribute("pairs", pairs);
		
		FileStorageService fs = FileStorageService.instance();
		BufferedImage imgData = ImageIO.read(new ByteArrayInputStream(fs
				.loadData(file.getHash())));
		
		if(parts.length > 2 && parts[2].equals("img")) {
			writeImage(file, pairs, request, response, false);
			return;
		}
		
		if(parts.length > 2 && parts[2].equals("printImg")) {
			writeImage(file, pairs, request, response, true);
			return;
		}
		
		request.setAttribute("imgWidth", imgData.getWidth());
		request.setAttribute("imgHeight", imgData.getHeight());

		context.getRequestDispatcher("/report.jsp").forward(request, response);
	}

	private void writeImage(UploadedFile file, List<ReportPair> pairs, HttpServletRequest request,
			HttpServletResponse response, boolean drawRects) throws IOException {
		FileStorageService fs = FileStorageService.instance();
		ByteArrayInputStream bis = new ByteArrayInputStream(fs
				.loadData(file.getHash()));
		if(!drawRects) {
			IOUtils.copy(bis, response.getOutputStream());
		} else {
			BufferedImage img = ImageIO.read(bis);
			Graphics2D g = img.createGraphics();
			g.setColor(new Color(0x40FF0000, true));
			for(ReportPair p : pairs) {
				Rectangle r1 = p.getFirst();
				Rectangle r2 = p.getSecond();
				g.fillRect(r1.x, r1.y, r1.width, r1.height);
				g.fillRect(r2.x, r2.y, r2.width, r2.height);
			}
			
			ImageIO.write(img, "PNG", response.getOutputStream());
		}
		response.getOutputStream().close();
	}
}
