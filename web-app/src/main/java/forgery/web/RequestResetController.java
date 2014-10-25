package forgery.web;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
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

public class RequestResetController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SessionFactory sessionFactory;

	public RequestResetController() {
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
		if (request.getParameter("email") != null) {
			String email = request.getParameter("email");

			Session session = sessionFactory.openSession();
			session.beginTransaction();

			User user = (User) session.createCriteria(User.class)
					.add(Restrictions.eq("mail", email)).uniqueResult();
			
			if(user == null) {
				request.setAttribute("noUserFound", true);
			} else {
				user.generateAuthToken();
				String authToken = user.getAuthToken();
				session.update(user);
				String hostName = request.getServerName();
				int port = request.getServerPort();
				String protocol = port == 443 ? "https://" : "http://";
				String fullHost = protocol + hostName + (port != 80 && port != 443 ? port : "");
				String link = fullHost + request.getContextPath() + "/index.html?authToken=" + authToken;
				System.out.println("Link to send: " + link);
				request.setAttribute("sentMail", true);
				MailService ms = MailService.instance();
				try {
					ms.sendMailToUser(user, "passwordReset", hostName, link);
				} catch (MessagingException e) {
					request.setAttribute("sentMail", false);
				}
				
			}
			
			session.getTransaction().commit();
			session.close();

		}

		ServletContext context = getServletContext();
		context.getRequestDispatcher("/resetPw.jsp").forward(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ServletContext context = getServletContext();

		context.getRequestDispatcher("/resetPw.jsp").forward(request, response);
	}

}
