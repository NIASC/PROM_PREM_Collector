package servlet.core;

import java.util.logging.Level;

public interface _Logger {
	void log(String msg);
	void log(Level level, String msg);
	public void fatalLogAndAction(String msg);
	public void log(String msg, Exception e);
	public void log(Level level, String msg, Exception e);
}
