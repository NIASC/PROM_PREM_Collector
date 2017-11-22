package servlet.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import common.Utilities;

public class Crypto
{
	public static String decrypt(String messageEncrypted) throws NumberFormatException
	{
		String msg[] = messageEncrypted.split(":");
		byte b[] = new byte[msg.length];
		for (int i = 0; i < msg.length; ++i)
			b[i] = (byte) Integer.parseInt(msg[i], 16);
		
		return new String(decryptRSA(b));
	}

	private static final BigInteger d, n;
	
	static
	{
		BigInteger _d = null, _n = null;
		try {
			Properties props = new Properties();
			props.load(Utilities.getResourceStream(Crypto.class,
					"servlet/core/keys.ini"));
			_n = new BigInteger(props.getProperty("mod"), 16);
			_d = new BigInteger(props.getProperty("exp"), 16);
			props.clear();
		}
		catch (IOException | IllegalArgumentException _e) {
			
		}
		n = _n;
		d = _d;
	}
	
	private static byte[] decryptRSA(byte msgBytes[]) {
		return new BigInteger(msgBytes).modPow(d, n).toByteArray();
	}
}
