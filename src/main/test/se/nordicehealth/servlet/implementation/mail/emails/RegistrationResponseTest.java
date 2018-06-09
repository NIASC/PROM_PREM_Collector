package se.nordicehealth.servlet.implementation.mail.emails;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.implementation.mail.MessageGenerator;
import se.nordicehealth.servlet.implementation.mail.emails.EMail;
import se.nordicehealth.servlet.implementation.mail.emails.RegistrationResponse;

public class RegistrationResponseTest {
	RegistrationResponse rr;
	String _template = "Username: %s<br>Password: %s<br>";
	String template = String.format(_template, "PPC_REGRESP_USERNAME", "PPC_REGRESP_PASSWORD");
	String username = "kalkul007";
	String password = "s3cr3t";
	String email = "kalle.kula@ki.se";

	@Before
	public void setUp() throws Exception {
		rr = new RegistrationResponse(new MessageGenerator(template));
	}

	@Test
	public void testCreate() {
		EMail em = rr.create(username, password, email);
		String result = String.format(_template, username, password);
		Assert.assertEquals(result, em.getBody());
		Assert.assertEquals(Arrays.asList(email), em.getRecipients());
	}

}
