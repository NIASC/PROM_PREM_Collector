/** ServletConst.java
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
import java.net.URL;
import java.util.Properties;

import common.Utilities;

/**
 * This class contains useful constants for the servlet and loads the
 * serlvet settings file.
 * 
 * @author Marcus Malmquist
 *
 */
public abstract class ServletConst
{
	private static final String filePath = "servlet/core/settings.ini";
	
	public static final URL LOCAL_URL;
	public static final String LOG_DIR;
	public static final int LOG_SIZE, LOG_COUNT;
	
	static
	{
		URL url = null;
		String logdir = null;
		Integer logsize = null, logcount = null;
		try {
			Properties props = new Properties();
			props.load(Utilities.getResourceStream(ServletConst.class, filePath));
			logdir = props.getProperty("logdir");
			if (logdir.endsWith("/"))
				logdir = logdir.substring(0, logdir.length()-1);
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

	public static final String CMD_ADD_USER		= "add_user";
	public static final String CMD_ADD_CLINIC	= "add_clinic";
	public static final String CMD_RSP_REGISTR	= "respond_registration";
}
