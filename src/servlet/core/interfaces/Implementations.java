package servlet.core.interfaces;

import servlet.implementation.LocaleSE;
import servlet.implementation.MySQLDatabase;
import servlet.implementation.SHAEncryption;

public abstract class Implementations
{
	public static Database Database() {
		return MySQLDatabase.instance;
	}
	
	public static Encryption Encryption() {
		return SHAEncryption.instance;
	}
	
	public static Locale Locale() {
		return LocaleSE.SE;
	}
}
