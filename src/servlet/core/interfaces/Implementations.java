package servlet.core.interfaces;

import servlet.implementation.LocaleSE;
import servlet.implementation.SHAEncryption;

public abstract class Implementations
{
	public static Encryption Encryption() {
		return SHAEncryption.instance;
	}
	
	public static Locale Locale() {
		return LocaleSE.SE;
	}
}
