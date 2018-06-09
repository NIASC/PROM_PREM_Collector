package se.nordicehealth.zzphony.encryption;

import se.nordicehealth.servlet.core.PPCEncryption;

public class PhonyCrypto implements PPCEncryption {

	@Override
	public String decrypt(String messageEncrypted) throws NumberFormatException {
		return messageEncrypted;
	}

	@Override
	public String encrypt(String messagePlain) {
		return messagePlain;
	}

}
