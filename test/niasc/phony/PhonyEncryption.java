package niasc.phony;

import servlet.core.interfaces.Encryption;

public class PhonyEncryption implements Encryption {
	@Override public String generateNewSalt() { return ""; }
	@Override public String hashMessage(String message, String salt) { return message; }
	@Override public String hashMessage(String prepend, String message, String append) { return message; }
}
