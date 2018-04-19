package servlet.core;

public interface PPCStringScramble
{
	public String generateNewSalt();
	public String hashMessage(String message, String salt);
	public String hashMessage(String prepend, String message, String append);
}
