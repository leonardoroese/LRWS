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

public class Mail extends ConBase {
	private ServletConfig scontext = null;

	public Mail(ServletConfig sconf) {
		super(sconf);
		this.scontext = sconf;
	}

	public boolean sendMail(String to, String subj, String msg) {
		ServletContext ctx = this.scontext.getServletContext();

		Properties props = new Properties();
		props.put("mail.smtp.host", ctx.getInitParameter("mailHost"));
		props.put("mail.smtp.socketFactory.port", ctx.getInitParameter("mailPort"));
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", ctx.getInitParameter("mailAuth"));
		props.put("mail.smtp.port", ctx.getInitParameter("mailPort"));
		props.put("mail.smtp.socketFactory.port", ctx.getInitParameter("mailPort"));
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.starttls.enable", ctx.getInitParameter("mailTLS"));
		props.put("mail.smtp.ssl.enable", ctx.getInitParameter("mailSSL"));
		props.put("mail.smtp.user", ctx.getInitParameter("mailFrom"));
		// props.put("mail.debug", "true");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		SimpleAuth auth = null;
		auth = new SimpleAuth(ctx.getInitParameter("mailUser"), ctx.getInitParameter("mailPass"));
		Session session = Session.getInstance(props, auth);
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ctx.getInitParameter("mailFrom")));

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
