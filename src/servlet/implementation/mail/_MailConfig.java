package servlet.implementation.mail;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;

public interface _MailConfig {
	Message createEmptyMessage();
	Transport getTransport() throws NoSuchProviderException;
}
