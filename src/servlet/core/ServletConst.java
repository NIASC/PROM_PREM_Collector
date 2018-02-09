package servlet.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import common.Utilities;

public abstract class ServletConst
{
	private static final String filePath = "servlet/core/settings.ini";
	
	public static final URL LOCAL_URL;
	public static final String LOG_DIR;
	public static final int LOG_SIZE, LOG_COUNT;
	
	static {
		URL url = null;
		String logdir = null;
		Integer logsize = null, logcount = null;
		try {
			Properties props = new Properties();
			props.load(Utilities.getResourceStream(ServletConst.class, filePath));
			logdir = props.getProperty("logdir");
			if (logdir.endsWith("/"))
				logdir = logdir.substring(0, logdir.length()-1);
			File logpath = new File(logdir);
			logpath.mkdirs();
			if (!logpath.exists()) {
				throw new IOException("Directory structure for logging could not be created. Verify that you have rwx rights.");
			}
			logsize = Integer.parseInt(props.getProperty("logsize"));
			logcount = Integer.parseInt(props.getProperty("logcount"));
			url = new URL(props.getProperty("localurl"));
			props.clear();
		} catch (IOException | IllegalArgumentException _e) {
			System.out.printf("FATAL: Could not load servlet settings file!");
			_e.printStackTrace(System.out);
			System.exit(1);
		}
		
		LOCAL_URL = url;
		LOG_DIR = logdir;
		LOG_SIZE = logsize.intValue();
		LOG_COUNT = logcount.intValue();
	}
}
