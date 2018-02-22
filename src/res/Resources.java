package res;

import java.io.InputStream;

public abstract class Resources {
	public static String ROOT_FOLDER = "res";
	public static String EMAIL_ACCOUNTS = ROOT_FOLDER + "/email_accounts.ini";
	public static String EMAIL_CONFIG = ROOT_FOLDER + "/email_settings.txt";
	public static String SETTINGS_PATH = ROOT_FOLDER + "/settings.ini";
	public static String KEY_PATH = ROOT_FOLDER + "/keys.ini";
	public static String REGREQ_EMAIL_BODY = ROOT_FOLDER + "/ppc_regreq.html";
	public static String REGRESP_EMAIL_BODY = ROOT_FOLDER + "/ppc_regresp.html";
	public static String MAIN_PAGE = ROOT_FOLDER + "/main.html";

	public static InputStream getStream(String filePath) {
		return Resources.class != null ? Resources.class.getClassLoader().getResourceAsStream(filePath) : null;
	}
}
