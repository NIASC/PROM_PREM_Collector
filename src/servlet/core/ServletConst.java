package servlet.core;

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

	public static enum _Packet {
		__NULL__,
		_TYPE,
		_DATA,
		__RESERVED0__,
		__RESERVED1__,
		__RESERVED2__,
		__RESERVED3__,
		__RESERVED4__,
		_ADMIN;
		
		public enum _Admin {
			__NULL__,
			YES,
			NO
		}
		
		public static enum _Types {
			__NULL__,
			GET_USER,
			GET_CLINICS,
			ADD_USER,
			ADD_CLINIC,
			RSP_REGISTR;
		}

		public enum _Data {
			__NULL__;
			
			public static enum _GetUser {
				__NULL__,
				USER,
				USERNAME;
				public static enum User {
					CLINIC_ID,
					USERNAME,
					PASSWORD,
					EMAIL,
					SALT,
					UPDATE_PASSWORD;
					public static enum UpdatePassword {
						YES,
						NO
					}
				}
			} // _GetUser
			
			public static enum _GetClinics {
				__NULL__,
				CLINICS
			} // _GetClinics
			
			public static enum _AddUser {
				__NULL__,
				RESPONSE,
				DETAILS;
				
				public static enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
				
				public static enum Details {
					__NULL__,
					CLINIC_ID,
					NAME,
					PASSWORD,
					EMAIL,
					SALT
				}
			} // _AddUser
			
			public static enum _AddClinic {
				__NULL__,
				RESPONSE,
				NAME;
				
				public static enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
			} // _AddClinic
			
			public static enum _RespondRegistration {
				__NULL__,
				RESPONSE,
				DETAILS;
				
				public static enum Response {
					__NULL__,
					FAIL,
					SUCCESS
				}
				
				public static enum Details {
					__NULL__,
					USERNAME,
					PASSWORD,
					EMAIL
				}
			} // _RespondRegistration
		} // _Packets
	}
}
