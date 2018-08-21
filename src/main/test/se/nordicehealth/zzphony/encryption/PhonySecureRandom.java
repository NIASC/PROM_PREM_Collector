package se.nordicehealth.zzphony.encryption;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;

@SuppressWarnings("serial")
public class PhonySecureRandom extends SecureRandom {

	public PhonySecureRandom() {
		// TODO Auto-generated constructor stub
	}

	public PhonySecureRandom(byte[] seed) {
		super(seed);
		// TODO Auto-generated constructor stub
	}

	public PhonySecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {
		super(secureRandomSpi, provider);
		// TODO Auto-generated constructor stub
	}
	
	public void nextBytes(byte[] bytes) {
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) 0xff;
		}
    }

}
