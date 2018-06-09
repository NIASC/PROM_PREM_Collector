package se.nordicehealth.servlet.implementation.exceptions;

public class DBReadException extends RuntimeException {
	public DBReadException() { super(); }
	public DBReadException(String message) { super(message); }
	private static final long serialVersionUID = 3557852594239271870L;
}
