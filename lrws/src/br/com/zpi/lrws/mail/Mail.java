package br.com.zpi.lrws.mail;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import br.com.zpi.lrws.conn.ConBase;
import br.com.zpi.lrws.conn.Configurations;

public class Mail extends ConBase {
	private Configurations conf = null;

	public Mail(Configurations conf) {
		super(conf);
		this.conf = conf;
	}

	public boolean sendMail(String to, String subj, String msg) {
		Properties props = new Properties();
		props.put("mail.smtp.host", conf.mailHost);
		props.put("mail.smtp.auth", conf.mailAuth);
		props.put("mail.smtp.port", conf.mailPort);
		props.put("mail.smtp.socketFactory.port", conf.mailPort);
		props.put("mail.smtp.starttls.enable", conf.mailTLS);
		if(conf.mailSSL != null && conf.mailSSL.trim().toUpperCase().equals("TRUE")){
			props.put("mail.smtp.ssl.enable", conf.mailSSL);
			props.put("mail.transport.protocol", "smtps");
			props.put("mail.smtps.ssl.checkserveridentity", "false");
			props.put("mail.smtps.ssl.trust", "*");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}else{
			props.put("mail.transport.protocol", "smtp");
		}
		props.put("mail.smtp.user", conf.mailFrom);
		props.put("mail.smtp.socketFactory.fallback", "false");
		// props.put("mail.debug", "true");

		Session session = null;
		if(conf.mailAuth != null && conf.mailAuth.trim().toUpperCase().equals("TRUE")){
			SimpleAuth auth = null;
			auth = new SimpleAuth(conf.mailUser, conf.mailPass);
			session = Session.getInstance(props, auth);
		}else{
			session = Session.getInstance(props);
		}
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(conf.mailFrom));

			Address[] toUser = InternetAddress.parse(to);
			message.setRecipients(Message.RecipientType.TO, toUser);
			message.setSubject(subj);
			message.setText(msg);
			message.setContent(msg, "text/html; charset=UTF-8");
			Transport.send(message);
			this.resType = "S";
			this.resMsg = "gen.mail.s.sent";
			return true;
		} catch (MessagingException e) {
			this.resType = "E";
			this.resMsg = "gen.mail.e.notsent";
			return false;
		}
	}
}

class SimpleAuth extends Authenticator {
	public String username = null;
	public String password = null;

	public SimpleAuth(String user, String pwd) {
		username = user;
		password = pwd;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}
}
