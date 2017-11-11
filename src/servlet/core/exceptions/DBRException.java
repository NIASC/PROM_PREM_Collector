package servlet.core.exceptions;

public class DBRException extends RuntimeException {

	public DBRException()
	{
		super();
	}
	
	public DBRException(String message)
	{
		super(message);
	}

	private static final long serialVersionUID = 667508701545850863L;


}
