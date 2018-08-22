package se.nordicehealth.servlet.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import se.nordicehealth.servlet.core.PPCFileHandlerUpdate;
import se.nordicehealth.servlet.core.PPCLogger;

public class ServletLogger implements PPCLogger {

	@Override
	public void log(Level level, String msg) {
		if (!currentLogfileRepresentsToday()) {
			updateHandlerIfNewDay();
		}
		logger.log(level, msg);
	}

	@Override
	public void log(String msg) {
		log(logger.getLevel(), msg);
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

	private final SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd");
	private Date currentDate = new Date(0L);
	private PPCFileHandlerUpdate handlerUpdater;
	private Handler handler;
	private Logger logger;
	
	public ServletLogger(PPCFileHandlerUpdate updater, Logger logger) {
		this.handlerUpdater = updater;
		this.logger = logger;
		log("Logger initialized successfully.");
	}
	
	private void updateHandlerIfNewDay() {
		try {
			setNewHandler(new Date(), handlerUpdater.updateHandler());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to create a log file. I/O error.");
		} catch (SecurityException e) {
			logger.log(Level.SEVERE, "Unable to create a log file. Make sure that the logging directory structure exists.");
		}
	}

	private boolean currentLogfileRepresentsToday() {
		return datefmt.format(currentDate).equals(datefmt.format(new Date()));
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
	
	private static class MyFormatter extends Formatter {
		private static final SimpleDateFormat datefmt = new SimpleDateFormat("HH:mm:ss");
		@Override
		public String format(LogRecord record) {
			return String.format("--> %s\n%s\n\n",
					datefmt.format(new Date(record.getMillis())), record.getMessage());
		}
	}
}