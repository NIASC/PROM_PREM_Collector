package servlet.core.interfaces;

import servlet.implementation.LocaleSE;
import servlet.implementation.MySQLDatabase;
import servlet.implementation.SHAEncryption;

public abstract class Implementations
{
	public static Database Database() {
		return MySQLDatabase.MYSQL;
	}
	
	public static Encryption Encryption() {
		return SHAEncryption.SHA;
	}
	
	public static Locale Locale() {
		return LocaleSE.SE;
	}
}
