package forgery.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.FileState;
import forgery.web.model.Report;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

/**
 * Servlet implementation class HomepageController
 */
public class HomepageController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SessionFactory sessionFactory;


	public HomepageController() {
		super();

	}
	
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
		if(request.getServletPath().equalsIgnoreCase("/logout")) {
			request.getSession().removeAttribute("user");
			response.sendRedirect("index.html");
			return;
		}
		if (request.getSession().getAttribute("user") != null) {
			User user = (User) request.getSession().getAttribute("user");
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<UploadedFile> userFiles = session
					.createCriteria(UploadedFile.class)
					.add(Restrictions.eq("user", user))
					.addOrder(Order.desc("created")).list();
			
			
			
			for(UploadedFile f : userFiles) {
				f.getReports().size();

			}
			
			
			session.getTransaction().commit();
			session.close();
			request.setAttribute("files", userFiles);
		}
		
		ServletContext context = getServletContext();

		context.getRequestDispatcher("/index.jsp").forward(request, response);
	}

}
