package niasc.phony.email;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

public class PhonyTransport extends Transport {

	public PhonyTransport(Session session, URLName url) {
		super(session, url);
	}

	@Override
	public void sendMessage(Message arg0, Address[] arg1) throws MessagingException {
		return;
	}
	
	@Override
	public void connect(String user, String password) {
		return;
	}
	
	@Override
	public void close() {
		return;
	}

}
