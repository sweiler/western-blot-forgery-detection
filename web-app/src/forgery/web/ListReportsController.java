package forgery.web;

import java.io.IOException;
import java.util.Arrays;

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

import forgery.web.model.Report;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

public class ListReportsController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SessionFactory sessionFactory;

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
		User user = (User) request.getSession().getAttribute("user");

		
		if(parts.length < 2 || parts[1] == null) {
			response.setStatus(400);
			response.getWriter().write("Task-ID required\n");
			response.getWriter().close();
			return;
		}
		int fileId = Integer.parseInt(parts[1]);
		

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		UploadedFile file = (UploadedFile) session.byId(UploadedFile.class).load(fileId);
		if(file == null) {
			session.getTransaction().commit();
			session.close();
			response.setStatus(400);
			response.getWriter().write("File-ID not found!\n");
			response.getWriter().close();
			return;
		}
		
		
		Report[] reports = (Report[]) file.getReports().toArray(new Report[file.getReports().size()]);
		
		Arrays.sort(reports);

		
		session.getTransaction().commit();
		session.close();

		
		
		if(user == null || file.getUser().getId() != user.getId()) {
			response.setStatus(403);
			response.getWriter().write("You are not the owner of this file!\n");
			response.getWriter().close();
			return;
		}
		
		request.setAttribute("reports", reports);
		request.setAttribute("file", file);


		context.getRequestDispatcher("/listReports.jsp").forward(request, response);
	}

}
