package servlet.implementation;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import common.Util;
import res.Resources;
import servlet.core.ServletLogger;

public class MailMan
{
	public static boolean sendRegReq(String name, String email, String clinic) {
		if (RegReqBodyTemplate == null) {
			logger.log(Level.WARNING, "The registration request email template was not loaded so the email could not be composed.");
			return false;
		}
		String emailSubject = "PROM/PREM: Registration request";
		String emailBody = RegReqBodyTemplate
				.replace("PPC_REGREQ_NAME", name)
				.replace("PPC_REGREQ_EMAIL", email)
				.replace("PPC_REGREQ_CLINIC", clinic);
		return send(adminEmail, emailSubject, emailBody, "text/html; charset=utf-8");
	}
	
	public static boolean sendRegResp(String username, String password, String email) {
		if (RegRespBodyTemplate == null) {
			logger.log(Level.WARNING, "The registration response email template was not loaded so the email could not be composed.");
			return false;
		}
		String emailSubject = "PROM/PREM: Registration response";
		String emailBody = RegRespBodyTemplate
				.replace("PPC_REGRESP_USERNAME", username)
				.replace("PPC_REGRESP_PASSWORD", password);
		return send(email, emailSubject, emailBody, "text/html; charset=utf-8");
	}

	private static final String RegReqBodyTemplate;
	private static final String RegRespBodyTemplate;
	private static Properties mailConfig;
	private static ServletLogger logger;
	private static String serverEmail, serverPassword, adminEmail;
	
	static {
		logger = ServletLogger.LOGGER;
		mailConfig = new Properties();
		try {
			refreshConfig();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not load email configuration", e);
			System.exit(1);
		}
		RegReqBodyTemplate = loadFile(Resources.REGREQ_EMAIL_BODY);
		RegRespBodyTemplate = loadFile(Resources.REGRESP_EMAIL_BODY);
	}
	
	private static String loadFile(String filename1) {
		String regreq = null;
		try {
			regreq = Util.fileToString(filename1, "UTF-8");
		} catch (IOException e) {
			logger.log(Level.SEVERE, String.format("Resource '%s' is not available.", Resources.REGREQ_EMAIL_BODY));
		} catch (UnsupportedCharsetException e) {
			logger.log(Level.SEVERE, String.format("Charset '%s' not available.", "UTF-8"));
		}
		return regreq;
	}
	
	private static synchronized void refreshConfig() throws IOException {
		loadConfig(Resources.EMAIL_CONFIG);
		loadEmailAccounts(Resources.EMAIL_ACCOUNTS);
	}
	
	private static synchronized void loadConfig(String filePath) throws IOException {
		if (!mailConfig.isEmpty()) {
			mailConfig.clear();
		}
		mailConfig.load(Resources.getStream(filePath));
	}
	
	private static synchronized void loadEmailAccounts(String filePath) throws IOException {
		Properties props = new Properties();
		props.load(Resources.getStream(filePath));
		adminEmail = props.getProperty("admin_email");
		serverEmail = props.getProperty("server_email");
		serverPassword = props.getProperty("server_password");
		props.clear();
	}

	private static boolean send(String recipient, String emailSubject, String emailBody, String bodyFormat)
	{
		try {
			/* generate session and message instances */
			Session s = Session.getDefaultInstance(mailConfig, null);
			
			/* create email */
			MimeMessage msg = new MimeMessage(s);
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			msg.setSubject(emailSubject);
			msg.setContent(emailBody, bodyFormat);
			
			/* login to server email account and send email. */
			Transport transport = s.getTransport();
			transport.connect(serverEmail, serverPassword);
			transport.sendMessage(msg, msg.getAllRecipients());
			transport.close();
			return true;
		} catch (MessagingException me) {
			logger.log("Could not send email", me);
			return false;
		}
	}
}
