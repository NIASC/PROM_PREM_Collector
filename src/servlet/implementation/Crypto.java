package servlet.implementation;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import res.Resources;

public class Crypto
{
	public static String decrypt(String messageEncrypted) throws NumberFormatException
	{
		String msg[] = messageEncrypted.split(":");
		byte b[] = new byte[msg.length];
		for (int i = 0; i < msg.length; ++i) {
			b[i] = (byte) Integer.parseInt(msg[i], 16);
		}
		return new String(decryptRSA(b));
	}

	private static final BigInteger d, n;
	
	static {
		BigInteger pow = null, mod = null;
		try {
			Properties props = new Properties();
			props.load(Resources.getStream(Resources.KEY_PATH));
			mod = new BigInteger(props.getProperty("mod"), 16);
			pow = new BigInteger(props.getProperty("exp"), 16);
			props.clear();
		} catch (IOException _e) { } catch (IllegalArgumentException _e) { }
		n = mod;
		d = pow;
	}
	
	private static byte[] decryptRSA(byte msgBytes[]) {
		return new BigInteger(msgBytes).modPow(d, n).toByteArray();
	}
}
