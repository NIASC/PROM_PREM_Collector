package niasc.servlet.implementation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import niasc.phony.encryption.PhonyMessageDigest;
import niasc.phony.encryption.PhonySecureRandom;
import servlet.implementation.SHAEncryption;
import servlet.implementation.User;

public class UserTest {
	User user;
	String raw_password = "s3cr3t";
	String hashed_password = "0000000000000000000000000000000000000000000000000000"
			+ (String.format("%02x", (int)'s')) + (String.format("%02x", (int)'3'))
			+ (String.format("%02x", (int)'c')) + (String.format("%02x", (int)'r'))
			+ (String.format("%02x", (int)'3')) + (String.format("%02x", (int)'t'));

	@Before
	public void setUp() throws Exception {
		user = new User(new SHAEncryption(new PhonySecureRandom(), new PhonyMessageDigest("SHA-256")));
		user.clinic_id = 1;
		user.email = "email";
		user.name = "name";
		user.password = hashed_password;
		user.salt = "";
		user.update_password = false;
	}

	@Test
	public void testClone() {
		User newUser = (User) user.clone();
		Assert.assertNotSame(newUser, user);
		Assert.assertEquals(newUser.clinic_id, user.clinic_id);
		Assert.assertEquals(newUser.email, user.email);
		Assert.assertEquals(newUser.name, user.name);
		Assert.assertEquals(newUser.password, user.password);
		Assert.assertEquals(newUser.salt, user.salt);
		Assert.assertEquals(newUser.update_password, user.update_password);
	}

	@Test
	public void testPasswordMatches() {
		Assert.assertTrue(user.passwordMatches(raw_password));
	}

	@Test
	public void testHashWithSalt() {
		Assert.assertEquals(hashed_password, user.hashWithSalt(raw_password));
	}

}
