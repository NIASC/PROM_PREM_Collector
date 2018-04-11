package servlet.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public enum ServletLogger implements _Logger {
	LOGGER;

	@Override
	public void log(String msg) {
		log(logger.getLevel(), msg);
	}

	@Override
	public void log(Level level, String msg) {
		updateHandlerIfNewDay();
		logger.log(level, msg);
	}
	
	/**
	 * Error was fatal and program can not continue executing.
	 * Log and exit.
	 * @param msg
	 */
	@Override
	public void fatalLogAndAction(String msg) {
		log(Level.SEVERE, "!!FATAL!! " + msg);
		System.exit(1);
	}

	@Override
	public void log(String msg, Exception e) {
		log(logger.getLevel(), msg, e);
	}

	@Override
	public void log(Level level, String msg, Exception e) {
		StringBuilder sb = new StringBuilder();
		if (msg != null && !msg.trim().isEmpty()) {
			sb.append(msg + "\n");
		}
		sb.append(e.toString() + "\n");
		for (StackTraceElement ste : e.getStackTrace()) {
			sb.append("\tat " + ste.toString() + "\n");
		}
		log(level, sb.toString());
	}
	
	private static final SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat serverStartFMT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	private static final Date serverStart;
	private static Date currentDate = new Date(0L);
	private Handler handler;
	private Logger logger;
	
	static {
		serverStart = new Date();
	}
	
	private ServletLogger() {
		logger = Logger.getLogger(ServletLogger.class.getName());
		logger.setLevel(Level.FINEST);
	}
	
	private void updateHandlerIfNewDay() {
		if (currentLogfileRepresentsToday()) {
			return;
		}
		
		Date today = new Date();
		Handler todayHandler = null;
		try {
			String outputFilePattern = String.format("%s/%s.log", ServletConst.LOG_DIR, generateLogfileName(today));
			todayHandler = new FileHandler(outputFilePattern, ServletConst.LOG_SIZE, ServletConst.LOG_COUNT);
		} catch (IOException e) { } catch (SecurityException e) { }

		if (todayHandler != null) {
			setNewHandler(today, todayHandler);
		} else {
			logger.log(Level.SEVERE, "Unable to create a log file. Make sure that the logging directory structure exists.");
		}
	}

	private String generateLogfileName(Date today) {
		return String.format("Servlet_%s@%s", serverStartFMT.format(serverStart), datefmt.format(today)) ;
	}

	private void setNewHandler(Date today, Handler todayHandler) {
		for (Handler h : logger.getHandlers()) {
			logger.removeHandler(h);
		}
		currentDate = today;
		handler = todayHandler;
		handler.setFormatter(new MyFormatter());
		logger.addHandler(handler);
	}

	private boolean currentLogfileRepresentsToday() {
		return datefmt.format(currentDate).equals(datefmt.format(new Date()));
	}
	
	private static class MyFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return String.format("--> %s\n%s\n\n",
					datefmt.format(new Date(record.getMillis())), record.getMessage());
		}
		
		private static SimpleDateFormat datefmt = new SimpleDateFormat("HH:mm:ss");
	}
}