/** PPCLogger.java
 * 
 * Copyright 2017 Marcus Malmquist
 * 
 * This file is part of PROM_PREM_Collector.
 * 
 * PROM_PREM_Collector is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * PROM_PREM_Collector is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PROM_PREM_Collector.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
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

/**
 * This class handles logging error messages on the servelt.
 * 
 * @author Marcus Malmquist
 *
 */
public class PPCLogger
{

	/**
	 * Retrieves the active instance of the logger.
	 * 
	 * @return The active instance of the logger.
	 */
	public static synchronized PPCLogger getLogger()
	{
		if (instance == null)
			instance = new PPCLogger();
		return instance;
	}

	@Override
	public final Object clone()
			throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	/**
	 * Writes {@code msg} to the log file.
	 * 
	 * @param msg The message to write to the log file.
	 */
	public void log(String msg)
	{
		log(logger.getLevel(), msg);
	}
	
	/**
	 * Writes {@code msg} to the log file specifying the level.
	 * 
	 * @param level The level of the event.
	 * @param msg The message to write to the log file
	 */
	public void log(Level level, String msg)
	{
		updateHandler();
		logger.log(level, msg);
	}
	
	/**
	 * Writes {@code msg} followed by the stack trace for {@code e}
	 * to the log file.
	 * 
	 * @param msg The message to write to the log file.
	 * @param e The {@code Exception} that was caught.
	 */
	public void log(String msg, Exception e)
	{
		log(logger.getLevel(), msg, e);
	}
	
	/**
	 * Writes {@code msg} followed by the stack trace for {@code e}
	 * to a log file.
	 * 
	 * @param level The level of the event.
	 * @param msg The message to write to the log file
	 * @param e The {@code Exception} that was caught.
	 */
	public void log(Level level, String msg, Exception e)
	{
		StringBuilder sb = new StringBuilder();
		if (msg != null && !msg.trim().isEmpty())
			sb.append(msg + "\n");
		sb.append(e.toString() + "\n");
		for (StackTraceElement ste : e.getStackTrace())
			sb.append("\tat " + ste.toString() + "\n");
		log(level, sb.toString());
	}
	
	
	private static PPCLogger instance;
	
	private static SimpleDateFormat datefmt;
	private static Date currentDate;
	private Handler handler;
	private Logger logger;
	
	static {
		currentDate = new Date(0L);
		datefmt = new SimpleDateFormat("yyyy-MM-dd");
	}
	
	/**
	 * Created a logger that uses this class as its name and
	 * sets the default level to Level.FINEST.
	 */
	private PPCLogger()
	{
		logger = Logger.getLogger(PPCLogger.class.getName());
		logger.setLevel(Level.FINEST);
	}
	
	/**
	 * Checks if the handler (i.e. log file name) needs to be updated.
	 * The log files are named by the date at which they occur so the
	 * handler should be updated every time a logging is done, but
	 * limited to once a day.
	 */
	private void updateHandler()
	{
		if (datefmt.format(currentDate).equals(datefmt.format(new Date())))
			return;
		
		for (Handler h : logger.getHandlers())
			logger.removeHandler(h);
		
		try
		{
			currentDate = new Date();
			String pattern = String.format(
					ServletConst.LOG_DIR + "/%s.log",
					datefmt.format(currentDate));
			handler = new FileHandler(pattern, ServletConst.LOG_SIZE,
					ServletConst.LOG_COUNT);
			handler.setFormatter(new MyFormatter());
			logger.addHandler(handler);
		} catch (SecurityException | IOException e) { }
	}
	
	/**
	 * A formatter that formats log entries according to the template:<br>
	 * <code>
	 * --&gt; date<br>
	 * messsage<br>
	 * ...<br>
	 * --&gt; date<br>
	 * messsage
	 * </code>
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private static class MyFormatter extends Formatter
	{
		@Override
		public String format(LogRecord record) {
			return String.format("--> %s\n%s",
					datefmt.format(new Date(record.getMillis())),
					record.getMessage());
		}
		
		private static SimpleDateFormat datefmt;
		
		static {
			datefmt = new SimpleDateFormat("HH:mm:ss");
		}
	}
}