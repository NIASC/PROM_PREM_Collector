package se.nordicehealth.servlet.impl.mail.emails;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.mail.emails.EMail;

public class EMailTest {
	EMail email;
	List<String> recipients = Arrays.asList("admin@ki.se", "user@ki.se");
	String subject = "Test Email";
	String body = "Hello!<br>Goodbye!";
	String format = "text/html";

	@Before
	public void setUp() throws Exception {
		email = new EMail(recipients, subject, body, format);
	}

	@Test
	public void testGetRecipients() {
		Assert.assertEquals(recipients, email.getRecipients());
	}

	@Test
	public void testGetSubject() {
		Assert.assertEquals(subject, email.getSubject());
	}

	@Test
	public void testGetBody() {
		Assert.assertEquals(body, email.getBody());
	}

	@Test
	public void testGetFormat() {
		Assert.assertEquals(format, email.getFormat());
	}

}
