package servlet.implementation.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Session;

import res.Resources;
import servlet.core._Logger;

public class MailManFactory {
	
	public static MailMan newInstance(String accountsFile, String configFile, _Logger logger) throws IOException {
		EmailAccounts ea = loadEmailAccounts(accountsFile);
		_MailConfig emcfg = loadEmailConfig(configFile);
		return newInstance(new ArrayList<String>(0), new Credentials(ea.serverEmail, ea.serverPassword), emcfg, logger);
	}
	
	public static MailMan newAdminInstance(String accountsFile, String configFile, _Logger logger) throws IOException {
		EmailAccounts ea = loadEmailAccounts(accountsFile);
		_MailConfig emcfg = loadEmailConfig(configFile);
		return newInstance(Arrays.asList(ea.adminEmail), new Credentials(ea.serverEmail, ea.serverPassword), emcfg, logger);
	}
	
	private static MailMan newInstance(List<String> defaultRecipient, Credentials cred, _MailConfig emcfg, _Logger logger) {
		return new MailMan(emcfg, cred, defaultRecipient, logger);
	}

	private static _MailConfig loadEmailConfig(String filename) throws IOException {
		Properties emailCfg = new Properties();
		emailCfg.load(Resources.getStream(filename));
		return new MailConfig(Session.getDefaultInstance(emailCfg, null));
	}

	private static EmailAccounts loadEmailAccounts(String filename) throws IOException {
		Properties props = new Properties();
		props.load(Resources.getStream(filename));
		return new EmailAccounts(
				props.getProperty("admin_email"),
				props.getProperty("server_email"),
				props.getProperty("server_password"));
	}
	
	private static class EmailAccounts {
		String adminEmail, serverEmail, serverPassword;
		EmailAccounts(String adminEmail, String serverEmail, String serverPassword) {
			this.adminEmail = adminEmail;
			this.serverEmail = serverEmail;
			this.serverPassword = serverPassword;
		}
	}
}
