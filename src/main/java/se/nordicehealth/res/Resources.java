package se.nordicehealth.res;

import java.io.InputStream;

public abstract class Resources {
	private static final String ROOT_FOLDER = "se/nordicehealth/res";
	private static final String ResourcePath(String file) { return ROOT_FOLDER + file; }
	
	public static final String
	REGRESP_EMAIL_BODY_TEMPLATE = ResourcePath("/ppc_regresp.html"),
	REGREQ_EMAIL_BODY_TEMPLATE = ResourcePath("/ppc_regreq.html"),
	EMAIL_ACCOUNTS_CONFIG = ResourcePath("/email_accounts.ini"),
	SETTINGS_CONFIG = ResourcePath("/settings.ini"),
	EMAIL_CONFIG = ResourcePath("/email_settings.txt"),
	KEY_CONFIG = ResourcePath("/keys.ini"),
	MAIN_PAGE = ResourcePath("/main.html");

	public static InputStream getStream(String filePath) {
		return Resources.class != null ? Resources.class.getClassLoader().getResourceAsStream(filePath) : null;
	}
}
