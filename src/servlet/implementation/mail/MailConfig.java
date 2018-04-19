package servlet.implementation.mail;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class MailConfig implements IMailConfig {
	public MailConfig(Session session) {
		loadConfig(session);
	}
	
	public synchronized void loadConfig(Session session) {
		this.session = session;
	}
	
	@Override
	public synchronized Message createEmptyMessage() {
		return new MimeMessage(session);
	}
	
	@Override
	public synchronized Transport getTransport() throws NoSuchProviderException {
		return session.getTransport();
	}
	
	private Session session;
}
