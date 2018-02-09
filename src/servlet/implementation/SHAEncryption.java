package servlet.implementation;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import servlet.core.interfaces.Encryption;

public enum SHAEncryption implements Encryption
{
	SHA;

	@Override
	public String hashMessage(String s, String salt) {
		return hashMessage("", s, salt);
	}

	@Override
	public String generateNewSalt() {
    	byte[] salt = new byte[8];
    	sr.nextBytes(salt);
		return String.format("%016x", new BigInteger(1, salt));
	}
	
	@Override
	public String hashMessage(String prepend, String message, String append) {
		String messageDigest = prepend + message + append;
		return String.format("%064x", new BigInteger(1,
				md.digest(messageDigest.getBytes())));
	}
	
	private SecureRandom sr;
	private MessageDigest md;
	
	private SHAEncryption() throws NullPointerException {
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new NullPointerException(String.format(
					"WARNING: Hashing algorithms %s and/or %s is not "
					+ "available. You should not add sensitive information "
					+ "to the database.", "SHA1PRNG", "SHA-256"));
		}
	}
}
