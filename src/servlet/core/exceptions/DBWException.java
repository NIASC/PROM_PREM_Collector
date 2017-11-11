package servlet.core.exceptions;

public class DBWException extends RuntimeException {


	public DBWException()
	{
		super();
	}
	
	public DBWException(String message)
	{
		super(message);
	}
	
	private static final long serialVersionUID = -8335182649112071418L;
}
