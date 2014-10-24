package forgery.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.User;

public class UserStateController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static SessionFactory sessionFactory;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		if (user == null) {
			response.getWriter().write("false");
			response.getWriter().close();
			return;
		}
		
		if(user.getMail() == null && user.getAuthToken() != null)
			response.getWriter().write(user.getAuthToken());
		else
			response.getWriter().write("false");
		response.getWriter().close();
	}

	@Override
	public void destroy() {
		super.destroy();
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
	
	

}
