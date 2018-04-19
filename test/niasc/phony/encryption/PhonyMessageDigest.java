package niasc.phony.encryption;

import java.security.MessageDigest;

public class PhonyMessageDigest extends MessageDigest {

	public PhonyMessageDigest(String algorithm) {
		super(algorithm);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void engineUpdate(byte input) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void engineUpdate(byte[] input, int offset, int len) {
		// TODO Auto-generated method stub

	}

	@Override
	protected byte[] engineDigest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void engineReset() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public byte[] digest(byte[] input) {
		return input;
    }
}
