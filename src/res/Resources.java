package res;

import java.io.InputStream;

public interface Resources {
	String ROOT_FOLDER = "res";
	String EMAIL_ACCOUNTS = ROOT_FOLDER + "/email_accounts.ini";
	String EMAIL_CONFIG = ROOT_FOLDER + "/email_settings.txt";
	String SETTINGS_PATH = ROOT_FOLDER + "/settings.ini";
	String KEY_PATH = ROOT_FOLDER + "/keys.ini";
	String REGREQ_EMAIL_BODY = ROOT_FOLDER + "/ppc_regreq.html";
	String REGRESP_EMAIL_BODY = ROOT_FOLDER + "/ppc_regresp.html";
	String MAIN_PAGE = ROOT_FOLDER + "/main.html";

	static InputStream getStream(String filePath) {
		return Resources.class != null ? Resources.class.getClassLoader().getResourceAsStream(filePath) : null;
	}
}
