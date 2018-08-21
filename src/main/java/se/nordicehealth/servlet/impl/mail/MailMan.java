package se.nordicehealth.servlet.impl.mail;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import se.nordicehealth.common.Util;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.mail.emails.EMail;

public class MailMan {

	public MailMan(IMailConfig cfg, Credentials cred, List<String> defaultRecipients, PPCLogger logger) {
		this.cfg = cfg;
		this.defaultRecipients = defaultRecipients;
		this.logger = logger;
		updateCredentials(cred);
	}
	
	public void updateCredentials(Credentials cred) {
		this.cred = cred;
	}

	public boolean send(EMail email) {
		try {
			send(createMessage(Util.joinLists(defaultRecipients, email.getRecipients()), email.getSubject(), email.getBody(), email.getFormat()));
			return true;
		} catch (MessagingException me) {
			me.printStackTrace();
			logger.log("Could not send email", me);
			return false;
		}
	}

	private Message createMessage(List<String> recipients, String subject, String body, String bodyFormat) throws AddressException, MessagingException {
		Message msg = cfg.createEmptyMessage();
		for (String recipient : recipients) {
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}
		msg.setSubject(subject);
		msg.setContent(body, bodyFormat);
		return msg;
	}
	
	private void send(Message msg) throws MessagingException {
		Transport t = cfg.getTransport();
		t.connect(cred.getEmail(), cred.getPassword());
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();
	}
	
	private PPCLogger logger;
	private IMailConfig cfg;
	private Credentials cred;
	private List<String> defaultRecipients;
}
