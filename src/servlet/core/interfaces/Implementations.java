package servlet.core.interfaces;

import servlet.implementation.SHAEncryption;

public abstract class Implementations
{
	public static Encryption Encryption() {
		return SHAEncryption.instance;
	}
}
