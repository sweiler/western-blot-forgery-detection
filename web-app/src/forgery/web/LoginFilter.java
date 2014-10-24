package forgery.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import forgery.web.model.Report;
import forgery.web.model.UploadedFile;
import forgery.web.model.User;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "loginFilter", dispatcherTypes = {
		DispatcherType.REQUEST, DispatcherType.FORWARD }, urlPatterns = { "/*" })
public class LoginFilter implements Filter {

	private SessionFactory sessionFactory;

	public void destroy() {
		sessionFactory.close();
	}

	public void doFilter(ServletRequest requestX, ServletResponse responseX,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) requestX;
		HttpServletResponse response = (HttpServletResponse) responseX;

		User user = (User) request.getSession().getAttribute("user");
		request.setAttribute("email", "");

		if (request.getParameter("authToken") != null) {
			Session session = sessionFactory.openSession();
			session.beginTransaction();

			User found_user = (User) session
					.createCriteria(User.class)
					.add(Restrictions.eq("authToken",
							request.getParameter("authToken"))).uniqueResult();

			session.getTransaction().commit();
			session.close();

			if (found_user != null) {
				request.getSession().setAttribute("user", found_user);
				if (found_user.getMail() != null) {
					response.sendRedirect(request.getContextPath() + "/profile");
					return;
				}
			}
		}

		if (request.getParameter("emailLogin") != null
				&& request.getParameter("passwordLogin") != null) {
			Session session = sessionFactory.openSession();
			session.beginTransaction();

			User found_user = (User) session
					.createCriteria(User.class)
					.add(Restrictions.eq("mail",
							request.getParameter("emailLogin"))).uniqueResult();

			if (found_user != null
					&& found_user.checkPwd(request
							.getParameter("passwordLogin"))) {
				user = found_user;
				User oldUser = (User) request.getSession().getAttribute("user");
				if (oldUser != null && oldUser.getMail() == null) {
					mergeUsers(session, user, oldUser);
				}
				request.getSession().setAttribute("user", user);
			} else {
				request.setAttribute("login_error", true);
				request.setAttribute("email",
						request.getParameter("emailLogin"));
			}
			session.getTransaction().commit();
			session.close();

		}

		if (user != null && request.getAttribute("locale") != null) {
			Session session = sessionFactory.openSession();
			session.beginTransaction();

			user = (User) session.merge(user);
			user.setLastUsedLocalization((String) request
					.getAttribute("locale"));
			session.update(user);

			session.getTransaction().commit();
			session.close();
		}

		if (user != null && user.getMail() != null)
			request.setAttribute("user", user);

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	public static void mergeUsers(Session session, User newUser, User oldUser) {
		@SuppressWarnings("unchecked")
		List<UploadedFile> files = (List<UploadedFile>) session
				.createCriteria(UploadedFile.class)
				.add(Restrictions.eq("user", oldUser)).list();
		
		newUser = (User) session.merge(newUser);
		oldUser = (User) session.merge(oldUser);
		for (UploadedFile f : files) {
			newUser.getFiles().add(f);
			oldUser.getFiles().remove(f);
			f.setUser(newUser);
			session.update(f);
		}
		session.getTransaction().commit();
		session.beginTransaction();
		oldUser = (User) session.load(User.class, oldUser.getId());
		session.delete(oldUser);
	}

	public void init(FilterConfig fConfig) throws ServletException {
		Configuration config = new Configuration();
		config.configure();
		ServiceRegistry reg = (new StandardServiceRegistryBuilder())
				.applySettings(config.getProperties()).build();
		sessionFactory = (new Configuration()).configure().buildSessionFactory(
				reg);
	}

}
