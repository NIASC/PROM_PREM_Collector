package se.nordicehealth.zzphony;

import se.nordicehealth.servlet.core.PPCStringScramble;

public class PhonyEncryption implements PPCStringScramble {
	@Override public String generateNewSalt() { return ""; }
	@Override public String hashMessage(String message, String salt) { return message; }
	@Override public String hashMessage(String prepend, String message, String append) { return message; }
}
