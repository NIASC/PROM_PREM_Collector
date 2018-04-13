package servlet.implementation;

public interface _Crypto {
	String decrypt(String messageEncrypted) throws NumberFormatException;
	String encrypt(String messagePlain);
}