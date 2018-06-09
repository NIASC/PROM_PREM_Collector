package se.nordicehealth.servlet.implementation.exceptions;

public class DBWriteException extends RuntimeException {
	public DBWriteException() { super(); }
	public DBWriteException(String message) { super(message); }
	private static final long serialVersionUID = -7992403920499044994L;

}
