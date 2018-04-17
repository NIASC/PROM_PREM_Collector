package niasc.servlet.implementation.mail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import niasc.phony.email.PhonyMessage;
import niasc.phony.email.PhonyTransport;
import servlet.core.PhonyLogger;
import servlet.implementation.mail.Credentials;
import servlet.implementation.mail.MailMan;
import servlet.implementation.mail._MailConfig;
import servlet.implementation.mail.emails.EMail;

public class MailManTest {
	MailMan mm;

	@Before
	public void setUp() throws Exception {
		_MailConfig _mc = new _MailConfig() {
			@Override
			public Message createEmptyMessage() { return new PhonyMessage(); }
			@Override
			public Transport getTransport() throws NoSuchProviderException {
				return new PhonyTransport(Session.getDefaultInstance(new Properties(), null), null);
			}
		};
		mm = new MailMan(_mc, new Credentials("", ""), new ArrayList<String>(0), new PhonyLogger());
	}

	@Test
	public void testSendEMail() {
		Assert.assertEquals(true, mm.send(new EMail(Arrays.asList("example@ki.se"), "", "", "")));
	}

}
