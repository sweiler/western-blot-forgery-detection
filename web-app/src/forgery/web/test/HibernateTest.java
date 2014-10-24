package forgery.web.test;

import static org.junit.Assert.*;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import forgery.web.model.User;

public class HibernateTest {
	private static SessionFactory sessionFactory;

	public HibernateTest() {

	}

	@BeforeClass
	public static void setUp() {
		Configuration config = new Configuration();
		config.configure();
		ServiceRegistry reg = (new StandardServiceRegistryBuilder())
				.applySettings(config.getProperties()).build();
		sessionFactory = (new Configuration()).configure().buildSessionFactory(
				reg);
	}

	@Test
	public void test() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(new User("mail@simon-weiler.de", "testPassword"));
		session.getTransaction().commit();
		session.close();
		
		session = sessionFactory.openSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<User> result = (List<User>) session.createQuery( "from User" ).list();
		for ( User user : result ) {
		    System.out.println( "User (" + user.getMail() + ") : " + user.getPwdHash() );
		}
		session.getTransaction().commit();
		session.close();
	}

}
