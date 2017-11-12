package servlet.core;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import common.Utilities;

public class MailMan
{
	public static boolean sendRegReq(
			String name, String email, String clinic)
	{
		String emailSubject = "PROM/PREM: Registration request";
		String emailDescription = "Registration reguest from";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s:<br><br> %s: %s<br>%s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Name", name, "E-mail",
				email, "Clinic", clinic, emailSignature);
		
		return send(adminEmail, emailSubject, emailBody, "text/html");
	}
	
	public static boolean sendRegResp(
			String username, String password, String email)
	{
		String emailSubject = "PROM/PREM: Registration response";
		String emailDescription = "You have been registered at the PROM/PREM Collector. "
				+ "You will find your login details below. When you first log in you will"
				+ "be asked to update your password.";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s<br><br> %s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Username", username,
				"Password", password, emailSignature);
		
		return send(email, emailSubject, emailBody, "text/html");
	}
	
	private static final String CONFIG_FILE =
			"servlet/implementation/email_settings.txt";
	private static final String ACCOUNT_FILE =
			"servlet/implementation/email_accounts.ini";
	private static Properties mailConfig;
	private static PPCLogger logger;
	
	// server mailing account
	private static String serverEmail, serverPassword, adminEmail;
	
	static
	{
		logger = PPCLogger.getLogger();
		mailConfig = new Properties();
		try {
			refreshConfig();
		} catch (IOException e) {
			logger.log("FATAL: Could not load email configuration", e);
			System.exit(1);
		}
	}
	
	/**
	 * reloads the javax.mail config properties as well as
	 * the email account config.
	 */
	private static synchronized void refreshConfig() throws IOException
	{
		loadConfig(CONFIG_FILE);
		loadEmailAccounts(ACCOUNT_FILE);
	}
	
	/**
	 * Loads the javax.mail config properties contained in the
	 * supplied config file.
	 * 
	 * @param filePath The file while the javax.mail config
	 * 		properties are located.
	 * 
	 * @return True if the file was loaded. False if an error
	 * 		occurred.
	 */
	private static synchronized void loadConfig(String filePath) throws IOException
	{
		if (!mailConfig.isEmpty())
			mailConfig.clear();
		mailConfig.load(Utilities.getResourceStream(MailMan.class, filePath));
	}
	
	/**
	 * Loads the registration program's email account information
	 * as well as the email address of the administrator who will
	 * receive registration requests.
	 * 
	 * @param filePath The file that contains the email account
	 * 		information.
	 * 
	 * @return True if the file was loaded. False if an error
	 * 		occurred.
	 */
	private static synchronized void loadEmailAccounts(String filePath) throws IOException
	{
		Properties props = new Properties();
		props.load(Utilities.getResourceStream(MailMan.class, filePath));
		adminEmail = props.getProperty("admin_email");
		serverEmail = props.getProperty("server_email");
		serverPassword = props.getProperty("server_password");
		props.clear();
	}

	/**
	 * Sends an email from the servlet's email account.
	 * 
	 * @param recipient The email address of to send the email to.
	 * @param emailSubject The subject of the email.
	 * @param emailBody The body/contents of the email.
	 * @param bodyFormat The format of the body. This could for
	 * 		example be 'text', 'html', 'text/html' etc.
	 */
	private static boolean send(String recipient, String emailSubject,
			String emailBody, String bodyFormat)
	{
		/* generate session and message instances */
		Session getMailSession = Session.getDefaultInstance(
				mailConfig, null);
		MimeMessage generateMailMessage = new MimeMessage(getMailSession);
		try
		{
			/* create email */
			generateMailMessage.addRecipient(Message.RecipientType.TO,
					new InternetAddress(recipient));
			generateMailMessage.setSubject(emailSubject);
			generateMailMessage.setContent(emailBody, bodyFormat);
			
			/* login to server email account and send email. */
			Transport transport = getMailSession.getTransport();
			transport.connect(serverEmail, serverPassword);
			transport.sendMessage(generateMailMessage,
					generateMailMessage.getAllRecipients());
			transport.close();
		} catch (MessagingException me)
		{
			logger.log("Could not send email", me);
			return false;
		}
		return true;
	}
}
