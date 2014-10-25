package forgery.web;

import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import forgery.util.Config;
import forgery.util.Utility;
import forgery.web.model.User;

public class MailService {
	
	private static MailService instance;
	
	private Session mailSession;
	
	private MailService() {
		Properties props = new Properties();
		Utility util = Utility.instance();
		final Config conf = util.getConfiguration();
		props.put("mail.host", conf.mailServer.trim());
		props.put("mail.user", conf.mailUser.trim());
		
		props.put("mail.auth", true);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		mailSession = Session.getInstance(props, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(conf.mailUser.trim(), conf.mailPassword.trim());
			}
		});
	}
	
	public static MailService instance() {
		if(instance == null)
			instance = new MailService();
		
		return instance;
	}
	
	public void sendPlainMail(String recipient, String subject, String message) throws MessagingException {
		Utility util = Utility.instance();
		final Config conf = util.getConfiguration();
		MimeMessage msg = new MimeMessage(mailSession);
		InternetAddress recp = new InternetAddress(recipient);
		msg.setFrom(new InternetAddress(conf.fromMail.trim()));
		msg.addRecipients(RecipientType.TO, new Address[]{recp});
		msg.setSubject(subject, "UTF-8");
        Multipart mp = new MimeMultipart();
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(message, "text/html;charset=utf-8");
        mp.addBodyPart(mbp);
        msg.setContent(mp);
        msg.setSentDate(new java.util.Date());

        Transport.send(msg);
	}
	
	public void sendMailToUser(User user, String messageKey, Object... params) throws MessagingException {
		Locale locale = new Locale("en");
		if(user.getLastUsedLocalization() != null) {
			locale = new Locale(user.getLastUsedLocalization());
		}
		I18N tr = new I18N("mail", locale);
		String subject = tr.__(messageKey + "_subject", params);
		String message = tr.__(messageKey + "_message", params);
		sendPlainMail(user.getMail(), subject, message);
	}
	

}
