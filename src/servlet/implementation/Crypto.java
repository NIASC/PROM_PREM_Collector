package servlet.implementation;

import java.math.BigInteger;
import java.util.Locale;

import servlet.core.PPCEncryption;

public class Crypto implements PPCEncryption
{
	private final BigInteger powPrivate, mod, powPublic;
	public Crypto(BigInteger powPrivate, BigInteger mod, BigInteger powPublic) {
		this.mod = mod;
		this.powPrivate = powPrivate;
		this.powPublic = powPublic;
	}
	
	@Override
	public String decrypt(String messageEncrypted) throws NumberFormatException {
		String msg[] = messageEncrypted.split(":");
		byte b[] = new byte[msg.length];
		for (int i = 0; i < msg.length; ++i) {
			b[i] = (byte) Integer.parseInt(msg[i], 16);
		}
		return new String(decryptRSA(b));
	}
	
	@Override
	public String encrypt(String messagePlain) {
		byte b[] = encryptRSA(messagePlain.getBytes());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; sb.append(i < b.length ? ":" : "")) {
			sb.append(String.format(Locale.US, "%02x", b[i++]));
		}
		return sb.toString();
	}
	
	private byte[] decryptRSA(byte msgBytes[]) {
		return new BigInteger(msgBytes).modPow(powPrivate, mod).toByteArray();
	}
	
	private byte[] encryptRSA(byte msgBytes[]) {
		return new BigInteger(msgBytes).modPow(powPublic, mod).toByteArray();
	}
}
