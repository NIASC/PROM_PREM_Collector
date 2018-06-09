package se.nordicehealth.servlet.core;

public interface PPCEncryption {
	String decrypt(String messageEncrypted) throws NumberFormatException;
	String encrypt(String messagePlain);
}