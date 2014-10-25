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

public class ProfileController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SessionFactory sessionFactory;

	public ProfileController() {
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
		if (request.getSession().getAttribute("user") == null) {
			response.setStatus(403);
			response.getWriter().write("No user logged in!");
			response.getWriter().close();
			return;
		}
		
		User user = (User) request.getSession().getAttribute("user");
		
		if(request.getParameter("delete") != null && request.getParameter("delete").equals("true")) {
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			
			user = (User) session.merge(user);
			
			session.delete(user);
			session.getTransaction().commit();
			session.close();
			
			request.getSession().removeAttribute("user");
			
			response.sendRedirect("index.html");
			return;
		}
		
		if (request.getParameter("email") != null && !request.getParameter("email").equals(user.getMail())) {
			
			user.setMail(request.getParameter("email"));
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			user = (User) session.merge(user);
			session.update(user);
			session.getTransaction().commit();
			session.close();

			request.setAttribute("changedMail", true);
		}
		
		if(request.getParameter("password") != null && request.getParameter("password").length() > 0) {
			if(request.getParameter("password").equals(request.getParameter("passwordRe"))) {
				
				Session session = sessionFactory.openSession();
				session.beginTransaction();
				user.setPassword(request.getParameter("password"));
				user.setAuthToken(null);
				user = (User) session.merge(user);
				
				session.update(user);
				session.getTransaction().commit();
				session.close();
				
				request.setAttribute("changedPw", true);
			} else {
				request.setAttribute("passwordEqual", false);
			}
		}
		
		

		ServletContext context = getServletContext();
		context.getRequestDispatcher("/profile.jsp")
				.forward(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();
		if (request.getSession().getAttribute("user") == null) {
			response.setStatus(403);
			response.getWriter().write("No user logged in!");
			response.getWriter().close();
			return;
		}
		
		

		context.getRequestDispatcher("/profile.jsp")
				.forward(request, response);
	}

}
