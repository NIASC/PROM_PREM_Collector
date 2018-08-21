package se.nordicehealth.servlet.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

import se.nordicehealth.servlet.core.PPCStringScramble;

public class SHAEncryption implements PPCStringScramble {
	
	public SHAEncryption(SecureRandom sr, MessageDigest md) throws NullPointerException {
		this.sr = sr;
		this.md = md;
	}

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
}
