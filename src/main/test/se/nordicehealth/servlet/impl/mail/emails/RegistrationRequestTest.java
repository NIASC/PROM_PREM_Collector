package se.nordicehealth.servlet.impl.mail.emails;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.mail.MessageGenerator;
import se.nordicehealth.servlet.impl.mail.emails.EMail;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationRequest;

public class RegistrationRequestTest {
	RegistrationRequest rr;
	String _template = "Name: %s<br>Email: %s<br>Clinic: %s<br>";
	String template = String.format(_template, "PPC_REGREQ_NAME", "PPC_REGREQ_EMAIL", "PPC_REGREQ_CLINIC");
	String name = "kalle";
	String email = "kalle@ki.se";
	String clinic = "Dummy";

	@Before
	public void setUp() throws Exception {
		rr = new RegistrationRequest(new MessageGenerator(template));
	}

	@Test
	public void testCreate() {
		EMail em = rr.create(name, email, clinic);
		String result = String.format(_template, name, email, clinic);
		Assert.assertEquals(result, em.getBody());
		Assert.assertEquals(new ArrayList<String>(0), em.getRecipients());
	}

}
