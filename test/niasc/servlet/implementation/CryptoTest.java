package niasc.servlet.implementation;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.implementation.Crypto;

public class CryptoTest {
	Crypto crypto;
	BigInteger mod        = new BigInteger("00d10944e092bf8ba8712c77321dbf52f5fd9879706a41596da17001c4a4c2d763630d482c95d20292978ef6be3e20a05d0cecc64d665975721c376c848a70fdebf36526807b02321e8c39004bda2800baf11e0b09ed1c83a3d146fa21d9b3f9f16c359f72b490b2917e21ddbadcdc1599709076320885a63158b4f70a3256cf2d", 16);
	BigInteger publicExp  = new BigInteger("10001", 16);
	BigInteger privateExp = new BigInteger("4214fa0f3c950d236cd3afc2ca20a7ab5846116df6493e6a27f4eeba2993e6df667ad66c31d8b8337b721892bfb534bcf5cf0c497fa79c373cb050bffdbc06a934316b2a750c146053cefb863b14da7e118f1084313bf536bfed5d64dc1c7c55d2a4ead7fd5df804490cd3a64e2e32ce71e58e84f4d25f4ec6e96fa04676edc9", 16);

	@Before
	public void setUp() throws Exception {
		crypto = new Crypto(privateExp, mod, publicExp);
	}

	@Test
	public void testDecrypt() {
		String message = "hello";
		String messageEncrypted = crypto.encrypt(message);
		Assert.assertNotEquals(message, messageEncrypted);
		String messageDecrypted = crypto.decrypt(messageEncrypted);
		Assert.assertEquals(message, messageDecrypted);
	}

}
