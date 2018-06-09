package se.nordicehealth.servlet;

import java.util.logging.Level;

import se.nordicehealth.servlet.core.PPCLogger;

public class LoggerForTesting implements PPCLogger {
	StringBuilder sb = new StringBuilder();
	public String getLogData() { return sb.toString(); }

	@Override
	public void log(String msg) { sb.append(msg + '\n'); }
	@Override
	public void log(Level level, String msg) { log(level.getName() + ": " + msg); }
	@Override
	public void fatalLogAndAction(String msg) { log(msg); }
	@Override
	public void log(String msg, Exception e) { log(msg + "; " + e.getMessage()); }
	@Override
	public void log(Level level, String msg, Exception e) { log(level.getName() + ": " + msg, e); }
}
