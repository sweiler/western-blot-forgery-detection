package forgery.web;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.mail.MessagingException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import forgery.web.model.ImageRect;
import forgery.web.model.FileState;
import forgery.web.model.FileType;
import forgery.web.model.Report;
import forgery.web.model.ReportPair;
import forgery.web.model.UploadedFile;

public class TaskController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SessionFactory sessionFactory;
	private FileStorageService fs;

	public TaskController() {
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

		fs = FileStorageService.instance();
	}

	@Override
	public void destroy() {
		sessionFactory.close();
	}

	private static final Queue<AsyncContext> queue = new ConcurrentLinkedQueue<AsyncContext>();

	public static void informTasks() throws IOException {
		System.out.println("informTasks() invoked");
		AsyncContext ctx = queue.poll();
		if(ctx != null) {
			((HttpServletResponse) ctx.getResponse())
			.sendRedirect(((HttpServletRequest) ctx.getRequest())
					.getContextPath() + "/tasks");
			ctx.complete();
		}
	}

	@Override
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo() == null ? "" : request
				.getPathInfo().replace("/", "");
		Session session = null;
		try {
			int taskId = Integer.parseInt(pathInfo);
			session = sessionFactory.openSession();
			session.beginTransaction();
			UploadedFile file = (UploadedFile) session.byId(UploadedFile.class).load(taskId);
			JSONParser parser = new JSONParser();
			
			
			JSONObject obj = (JSONObject) parser.parse(request.getReader());
			
			JSONArray pairs = (JSONArray) obj.get("pairs");
			
			int version = ((Long) obj.get("version")).intValue();
			String parameters = (String) obj.get("parameters");
			Report report = new Report(file, version, parameters);
			report.generateId();
			report.setCreated(new GregorianCalendar());
			session.save(report);
			
			List<ReportPair> repPairs = new ArrayList<ReportPair>();
			
			for (Object o : pairs) {
				JSONObject pair = (JSONObject) o;
				
				JSONObject first = (JSONObject) pair.get("first");
				JSONObject second = (JSONObject) pair.get("second");
				ReportPair rp = new ReportPair();
				rp.setFirst(unwrapRect(first));
				rp.setSecond(unwrapRect(second));
				rp.setReport(report);
				boolean unique = true;
				for(ReportPair other : repPairs) {
					if(other.getSecond().equals(rp.getFirst()) && other.getFirst().equals(rp.getSecond())) {
						unique = false;
						break;
					}
				}
				if(unique) {
					repPairs.add(rp);
					session.save(rp);
				}
			}
			
			
			
			file.setState(FileState.FINISHED);
			session.update(file);
			
			MailService.instance().sendMailToUser(file.getUser(), "notify", file.getFilename());
			
		} catch (NumberFormatException e) {

		} catch (ParseException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			if(session != null) {
				if(session.getTransaction() != null)
					session.getTransaction().commit();
				session.close();
			}
				
		}
	}
	
	private Rectangle unwrapRect(JSONObject obj)  {
		Rectangle res = new Rectangle();
		res.x = ((Long) obj.get("x")).intValue();
		res.y = ((Long) obj.get("y")).intValue();
		res.width = ((Long) obj.get("width")).intValue();
		res.height = ((Long) obj.get("height")).intValue();
		return res;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");
		String pathInfo = request.getPathInfo() == null ? "" : request
				.getPathInfo().replace("/", "");
		Writer w = response.getWriter();
		if (pathInfo.equals("")) {
			Session session = sessionFactory.openSession();
			session.beginTransaction();
			@SuppressWarnings("unchecked")
			List<UploadedFile> tasks = session
					.createCriteria(UploadedFile.class)
					.add(Restrictions.eq("state", FileState.START_ANALYZE))
					.add(Restrictions.disjunction()
							.add(Restrictions.eq("type", FileType.PNG))
							.add(Restrictions.eq("type", FileType.JPEG)))
					.addOrder(Order.asc("created")).setMaxResults(10).list();
			
			@SuppressWarnings("unchecked")
			List<UploadedFile> processingFiles = session
					.createCriteria(UploadedFile.class)
					.add(Restrictions.eq("state", FileState.PROCESSING))
					.list();
			
			for(UploadedFile file : processingFiles)
				FileStateController.checkFileForTimeout(session, file);
			
			session.getTransaction().commit();
			session.close();

			if (tasks.size() == 0) {
				final AsyncContext ctx = request.startAsync(request, response);
				ctx.setTimeout(10 * 60 * 1000);
				queue.offer(ctx);
				ctx.addListener(new AsyncListener() {
					public void onComplete(AsyncEvent event) throws IOException {
						queue.remove(ctx);
					}

					public void onTimeout(AsyncEvent event) throws IOException {
						queue.remove(ctx);
					}

					public void onError(AsyncEvent event) throws IOException {
						queue.remove(ctx);
					}

					public void onStartAsync(AsyncEvent event)
							throws IOException {
					}
				});
				

			} else {

				response.setStatus(200);

				w.append("{ \"status\" : \"ok\", \"num\" : " + tasks.size()
						+ ", \"tasks\" : [");
				boolean first = true;
				for (UploadedFile t : tasks) {
					String comma = first ? "" : ", ";
					w.append(comma + t.getId());
					first = false;
				}
				w.append("] }");
				w.close();
			}
		} else {
			try {
				int taskId = Integer.parseInt(pathInfo);
				Session session = sessionFactory.openSession();
				session.beginTransaction();
				UploadedFile task = (UploadedFile) session.byId(
						UploadedFile.class).load(taskId);

				@SuppressWarnings("unchecked")
				List<ImageRect> rects = session
						.createCriteria(ImageRect.class)
						.add(Restrictions.eq("file", task))
						.list();

				if (task == null) {
					response.setStatus(404);
					w.write("{\"status\" : \"nok\", \"message\" : \"Your requested task does not exist\"}");
					w.close();
				} else {
					byte[] data = fs.loadData(task.getHash());
					if (data == null) {
						response.setStatus(500);
						w.write("{\"status\" : \"nok\", \"message\" : \"File has no data\"}");
						w.close();
					} else {
						String filedata = Base64.encodeBase64String(data);
						boolean first = true;
						w.write("{\"status\" : \"ok\", \"task\" : {\n");
						w.write("\"id\" : " + taskId + ", \n");
						w.write("\"type\" : \"" + task.getType().name()
								+ "\", \n");
						w.write("\"rects\" : [");
						for (ImageRect rect : rects) {
							if (!first)
								w.write(",");
							w.write("{");
							w.write("\"x\" : " + rect.getX());
							w.write("\"y\" : " + rect.getY());
							w.write("\"width\" : " + rect.getWidth());
							w.write("\"height\" : " + rect.getHeight());
							w.write("}");
							first = false;
						}
						w.write("],\n");
						w.write("\"data\" : \"" + filedata + "\"");
						w.write("}}");
						w.close();
					}
				}

				task.setState(FileState.PROCESSING);
				session.update(task);

				session.getTransaction().commit();
				session.close();

			} catch (NumberFormatException e) {
				response.setStatus(400);
				w.append("{\"status\" : \"nok\", \"message\" : \"Your query string is malformed. Task id should be an integer.\"}");
				w.close();
			}

		}

	}


}
