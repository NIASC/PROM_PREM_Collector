package se.nordicehealth.servlet.implementation.mail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.implementation.mail.Credentials;

public class CredentialsTest {
	Credentials cred;
	String email = "example@email.com";
	String password = "s3cr3t";

	@Before
	public void setUp() throws Exception {
		cred = new Credentials(email, password);
	}

	@Test
	public void testGetEmail() {
		Assert.assertEquals(email, cred.getEmail());
	}

	@Test
	public void testGetPassword() {
		Assert.assertEquals(password, cred.getPassword());
	}

}
