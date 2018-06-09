package se.nordicehealth.servlet.implementation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.implementation.SHAEncryption;
import se.nordicehealth.zzphony.encryption.PhonyMessageDigest;
import se.nordicehealth.zzphony.encryption.PhonySecureRandom;

public class SHAEncryptionTest {
	SHAEncryption crypto;

	@Before
	public void setUp() throws Exception {
		crypto = new SHAEncryption(new PhonySecureRandom(), new PhonyMessageDigest("SHA-256"));
	}

	@Test
	public void testHashMessageStringString() {
		Assert.assertEquals("00000000000000000000000000000000000000000000000000000000"
				+ (String.format("%02x", (int)'t')) + (String.format("%02x", (int)'e'))
				+ (String.format("%02x", (int)'s')) + (String.format("%02x", (int)'t')),
				crypto.hashMessage("tes", "t"));
	}

	@Test
	public void testGenerateNewSalt() {
		Assert.assertEquals("ffffffffffffffff", crypto.generateNewSalt());
	}

	@Test
	public void testHashMessageStringStringString() {
		Assert.assertEquals("00000000000000000000000000000000000000000000000000000000"
				+ (String.format("%02x", (int)'t')) + (String.format("%02x", (int)'e'))
				+ (String.format("%02x", (int)'s')) + (String.format("%02x", (int)'t')),
				crypto.hashMessage("te", "s", "t"));
	}

}
