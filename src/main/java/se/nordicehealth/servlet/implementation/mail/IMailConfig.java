package se.nordicehealth.servlet.implementation.mail;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;

public interface IMailConfig {
	Message createEmptyMessage();
	Transport getTransport() throws NoSuchProviderException;
}
