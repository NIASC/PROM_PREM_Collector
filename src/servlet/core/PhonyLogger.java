package servlet.core;

import java.util.logging.Level;

public class PhonyLogger implements _Logger {

	public PhonyLogger() { }

	@Override
	public void log(String msg) { }

	@Override
	public void log(Level level, String msg) { }

	@Override
	public void fatalLogAndAction(String msg) { }

	@Override
	public void log(String msg, Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void log(Level level, String msg, Exception e) { }

}
