package forgery.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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

import forgery.web.model.UploadedFile;
import forgery.web.model.User;

/**
 * Servlet implementation class HomepageController
 */
public class RegisterController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SessionFactory sessionFactory;

	public RegisterController() {
		super();

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

	@Override
	public void destroy() {
		sessionFactory.close();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("email") != null
				&& request.getParameter("password") != null) {
			if(!request.getParameter("password").equals(request.getParameter("passwordRe"))) {
				request.setAttribute("passwordEqual", false);
			} else {
				request.setAttribute("passwordEqual", true);
				User new_user = new User();
				new_user.setMail(request.getParameter("email"));
				new_user.setPassword(request.getParameter("password"));
				Session session = sessionFactory.openSession();
				session.beginTransaction();
				session.persist(new_user);
				session.getTransaction().commit();
				session.close();

				User oldUser = (User) request.getSession().getAttribute("user");
				if(oldUser != null && oldUser.getMail() == null) {
					session = sessionFactory.openSession();
					session.beginTransaction();
		
					LoginFilter.mergeUsers(session, new_user, oldUser);
		
					session.getTransaction().commit();
					session.close();
				}
				request.getSession().setAttribute("user", new_user);
				response.sendRedirect("index.html");
				return;
			}
			
		}

		ServletContext context = getServletContext();
		context.getRequestDispatcher("/register.jsp")
				.forward(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();
		if (request.getSession().getAttribute("user") != null) {
			User user = (User) request.getSession().getAttribute("user");
			// TODO
		}

		context.getRequestDispatcher("/register.jsp")
				.forward(request, response);
	}

}
