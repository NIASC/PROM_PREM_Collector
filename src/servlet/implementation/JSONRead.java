/** JSONRead.java
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
package servlet.implementation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import common.Utilities;
import common.implementation.Constants;
import servlet.core.PPCLogger;
import servlet.core.ServletConst;
import servlet.core.UserManager;
import servlet.core._Message;
import servlet.core._Question;
import servlet.core._User;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Implementations;
import servlet.core.interfaces.Database.DatabaseFunction;

/**
 * This class handles redirecting a request from the applet to the
 * appropriate method in the servlet.
 * 
 * @author Marcus Malmquist
 *
 */
public class JSONRead
{
	private static PPCLogger logger = PPCLogger.getLogger();
	private static JSONParser parser;
	private static Map<String, DatabaseFunction> dbm;
	private static Database db;

	/**
	 * Configuration data for sending an email from the servlet's email
	 * to the admin's email.
	 */
	private static EmailConfig config;
	
	static {

		try {
			config = new EmailConfig();
		} catch (IOException e) {
			logger.log("FATAL: Could not load email configuration", e);
			System.exit(1);
		}
		
		parser = new JSONParser();
		dbm = new HashMap<String, DatabaseFunction>();
		db = Implementations.Database();

		dbm.put(ServletConst.CMD_ADD_USER, JSONRead::addUser);
		dbm.put(Constants.CMD_ADD_QANS, JSONRead::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, JSONRead::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, JSONRead::getClinics);
		dbm.put(Constants.CMD_GET_USER, JSONRead::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, JSONRead::setPassword);
		dbm.put(Constants.CMD_GET_ERR_MSG, JSONRead::getErrorMessages);
		dbm.put(Constants.CMD_GET_INFO_MSG, JSONRead::getInfoMessages);
		dbm.put(Constants.CMD_LOAD_Q, JSONRead::loadQuestions);
		dbm.put(Constants.CMD_LOAD_QR_DATE, JSONRead::loadQResultDates);
		dbm.put(Constants.CMD_LOAD_QR, JSONRead::loadQResults);
		dbm.put(Constants.CMD_REQ_REGISTR, JSONRead::requestRegistration);
		dbm.put(ServletConst.CMD_RSP_REGISTR, JSONRead::respondRegistration);
		dbm.put(Constants.CMD_REQ_LOGIN, JSONRead::requestLogin);
		dbm.put(Constants.CMD_REQ_LOGOUT, JSONRead::requestLogout);
	}
	
	/**
	 * Parses the {@code message} and redirects the request to the request
	 * to the appropriate method.
	 * 
	 * @param message The request from the applet.
	 * 
	 * @return The response from the servlet.
	 */
	@SuppressWarnings("unchecked")
	public static String handleRequest(String message)
	{
		try
		{
			JSONObject obj = (JSONObject) parser.parse(message);
			HashMap<String, String> omap = (HashMap<String, String>) obj;
			return getDBMethod(omap.get("command")).dbfunc(obj);
		}
		catch (ParseException pe) {
			logger.log("Unknown JSON format", pe);
		}
		catch (Exception e) {
			logger.log("Unknown request", e);
		}
		return null;
	}
	
	
	/**
	 * Finds the Method Reference associated with the {@code command}
	 * 
	 * @param command The command/method that is associated with a servlet
	 * 		method that shuld handle the request.
	 * 
	 * @return A reference to the servlet method that should handle the
	 * 		request.
	 * 
	 * @throws NullPointerException If no method exists that can handle
	 * 		the request.
	 */
	private static DatabaseFunction getDBMethod(String command)
	{
		return dbm.get(command);
	}
	
	private static String addUser(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_USER);
		
		boolean success = db.addUser(
				Integer.parseInt(in.jmap.get("clinic_id")),
				in.jmap.get("name"), in.jmap.get("password"),
				in.jmap.get("email"), in.jmap.get("salt"));
		if (success) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		} else {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}
	
	private static String addQuestionnaireAnswers(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_ADD_QANS);
		
		int clinic_id = Integer.parseInt(in.jmap.get("clinic_id"));
		String identifier = in.jmap.get("identifier");

		JSONMapData m = new JSONMapData(getJSONObject(in.jmap.get("questions")));
		List<String> question_ids = new ArrayList<String>();
		List<String> question_answers = new ArrayList<String>();
		for (Entry<String, String> e : m.jmap.entrySet()) {
			question_ids.add(e.getKey());
			question_answers.add(e.getValue());
		}
		
		if (db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, question_ids, question_answers)) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		} else {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}
	
	private static String addClinic(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_CLINIC);
		
		if (db.addClinic(in.jmap.get("name"))) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		} else {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}
	
	private static String getClinics(JSONObject obj)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_CLINICS);
			
		Map<Integer, String> _clinics = db.getClinics();
		JSONMapData clinics = new JSONMapData(null);
		for (Entry<Integer, String> e : _clinics.entrySet())
			clinics.jmap.put(Integer.toString(e.getKey()), e.getValue());
		out.jmap.put("clinics", clinics.jobj.toString());
		return out.jobj.toString();
	}
	
	private static String getUser(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_USER);

		_User _user = db._getUser(in.jmap.get("name"));
		JSONMapData user = new JSONMapData(null);
		if (_user != null) {
			user.jmap.put("clinic_id", Integer.toString(_user.clinic_id));
			user.jmap.put("name", _user.name);
			user.jmap.put("password", _user.password);
			user.jmap.put("email", _user.email);
			user.jmap.put("salt", _user.salt);
			user.jmap.put("update_password", _user.update_password ? "1" : "0");
			out.jmap.put("user", user.jobj.toString());
		} else {
			out.jmap.put("user", (new JSONObject()).toString());
		}
		return out.jobj.toString();
	}
	
	private static String setPassword(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);

		String name = in.jmap.get("name");
		String oldPass = in.jmap.get("old_password");
		String newPass = in.jmap.get("new_password");
		String newSalt = in.jmap.get("new_salt");

		db.setPassword(name, oldPass, newPass, newSalt);
		
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_USER);
		out.jmap.put("name", name);
		return getUser(out.jobj);
	}
	
	private static String getErrorMessages(JSONObject obj)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_ERR_MSG);
		out.jmap.put("messages", _getMessages(db.getErrorMessages()).toString());
		return out.jobj.toString();
	}
	
	private static String getInfoMessages(JSONObject obj)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_INFO_MSG);
		out.jmap.put("messages", _getMessages(db.getInfoMessages()).toString());
		return out.jobj.toString();
	}
	
	private static String loadQuestions(JSONObject obj)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_Q);
		
		Map<Integer, _Question> questions = db.loadQuestions();
		JSONMapData _questions = new JSONMapData(null);
		for (Entry<Integer, _Question> _e : questions.entrySet()) {
			_Question _q = _e.getValue();
			JSONMapData _question = new JSONMapData(null);
			int i = 0;
			for (String str : _q.options)
				_question.jmap.put(String.format("option%d", i++), str);
			_question.jmap.put("type", _q.type);
			_question.jmap.put("id", Integer.toString(_q.id));
			_question.jmap.put("question", _q.question);
			_question.jmap.put("description", _q.description);
			_question.jmap.put("optional", _q.optional ? "1" : "0");
			_question.jmap.put("max_val", Integer.toString(_q.max_val));
			_question.jmap.put("min_val", Integer.toString(_q.min_val));
			
			_questions.jmap.put(Integer.toString(_e.getKey()),
					_question.jobj.toString());
		}
		out.jmap.put("questions", _questions.jobj.toString());
		return out.jobj.toString();
	}
	
	private static String loadQResultDates(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR_DATE);

		_User user = db._getUser(in.jmap.get("name"));
		List<String> dlist = db.loadQResultDates(user.clinic_id);

		JSONArrData dates = new JSONArrData(null);
		for (String str : dlist)
			dates.jlist.add(str);
		out.jmap.put("dates", dates.jarr.toString());
		return out.jobj.toString();
	}
	
	private static String loadQResults(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR);

		_User _user = db._getUser(in.jmap.get("name"));
		JSONArrData questions = new JSONArrData(getJSONArray(in.jmap.get("questions")));

		List<Map<String, String>> _results = db.loadQResults(
				_user.clinic_id, questions.jlist,
				getDate(in.jmap.get("begin")),
				getDate(in.jmap.get("end")));

		JSONArrData results = new JSONArrData(null);
		for (Map<String, String> m : _results) {
			JSONMapData answers = new JSONMapData(null);
			for (Entry<String, String> e : m.entrySet())
				answers.jmap.put(e.getKey(), e.getValue());
			results.jlist.add(answers.jobj.toString());
		}
		out.jmap.put("results", results.jarr.toString());
		return out.jobj.toString();
	}

	/**
	 * Sends a registration request to an administrator.
	 * 
	 * @param obj The JSONObject that contains the request, including
	 * 		the name, clinic and email.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private static String requestRegistration(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_REGISTR);
		
		String name = in.jmap.get("name");
		String email = in.jmap.get("email");
		String clinic = in.jmap.get("clinic");
		
		String emailSubject = "PROM/PREM: Registration request";
		String emailDescription = "Registration reguest from";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s:<br><br> %s: %s<br>%s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Name", name, "E-mail",
				email, "Clinic", clinic, emailSignature);
		
		if (send(config.adminEmail, emailSubject, emailBody, "text/html"))
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		else
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		return out.jobj.toString();
	}

	/**
	 * Sends a registration responds that contains the login details
	 * to the user that have been registered.
	 * 
	 * @param obj The JSONObject that contains the request, including
	 * 		the usename and password.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private static String respondRegistration(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_RSP_REGISTR);
		
		String username = in.jmap.get("username");
		String email = in.jmap.get("email");
		String password = in.jmap.get("password");
		
		String emailSubject = "PROM/PREM: Registration response";
		String emailDescription = "You have been registered at the PROM/PREM Collector. "
				+ "You will find your login details below. When you first log in you will"
				+ "be asked to update your password.";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s<br><br> %s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Username", username,
				"Password", password, emailSignature);
		
		if (send(email, emailSubject, emailBody, "text/html"))
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		else
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		return out.jobj.toString();
	}

	/**
	 * Requests to log in.
	 * 
	 * @param obj The JSONObject that contains the request, including the
	 * 		username.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private static String requestLogin(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGIN);

		_User _user = db._getUser(in.jmap.get("name"));
		if (!_user.password.equals(in.jmap.get("password"))) {
			out.jmap.put(Constants.LOGIN_REPONSE, Constants.INVALID_DETAILS_STR);
			return out.jobj.toString();
		}
		
		UserManager um = UserManager.getUserManager();
		out.jmap.put(Constants.LOGIN_REPONSE, um.addUser(_user.name));
		return out.jobj.toString();
	}

	/**
	 * Requests to log out.
	 * 
	 * @param obj The JSONObject that contains the request, including the
	 * 		username.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private static String requestLogout(JSONObject obj)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGOUT);

		UserManager um = UserManager.getUserManager();
		String response = um.delUser(in.jmap.get("name")) ? Constants.SUCCESS_STR : Constants.ERROR_STR;
		out.jmap.put(Constants.LOGOUT_REPONSE, response);
		return out.jobj.toString();
	}
	
	// --------------------------------

	/**
	 * Attempts to parse {@code str} into a {@code JSONObject}.
	 * 
	 * @param str The string to be converted into a {@code JSONObject}.
	 * 
	 * @return The {@code JSONObject} representation of {@code str}, or
	 * 		{@code null} if {@code str} does not represent a
	 * 		{@code JSONObject}.
	 */
	private static JSONObject getJSONObject(String str)
	{
		try {
			return (JSONObject) parser.parse(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Attempts to parse {@code str} into a {@code JSONArray}.
	 * 
	 * @param str The string to be converted into a {@code JSONArray}.
	 * 
	 * @return The {@code JSONArray} representation of {@code str}, or
	 * 		{@code null} if {@code str} does not represent a
	 *  	{@code JSONArray}.
	 */
	private static JSONArray getJSONArray(String str)
	{
		try {
			return (JSONArray) parser.parse(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static JSONObject _getMessages(Map<String, _Message> _msg)
	{
		JSONMapData out = new JSONMapData(null);
		for (Entry<String, _Message> e : _msg.entrySet()) {
			_Message _message = e.getValue();
			
			JSONMapData msg = new JSONMapData(null);
			for (Entry<String, String> _e : _message.msg.entrySet()) {
				msg.jmap.put(_e.getKey(), _e.getValue());
			}

			JSONMapData message = new JSONMapData(null);
			message.jmap.put("name", _message.name);
			message.jmap.put("code", _message.code);
			message.jmap.put("message", msg.jobj.toString());

			out.jmap.put(e.getKey(), message.jobj.toString());
		}
		return out.jobj;
	}
	
	private static Date getDate(String date)
	{
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0L);
		}
	}

	/**
	 * Sends an email from the servlet's email account.
	 * 
	 * @param recipient The email address of to send the email to.
	 * @param emailSubject The subject of the email.
	 * @param emailBody The body/contents of the email.
	 * @param bodyFormat The format of the body. This could for
	 * 		example be 'text', 'html', 'text/html' etc.
	 */
	private static boolean send(String recipient, String emailSubject,
			String emailBody, String bodyFormat)
	{
		/* generate session and message instances */
		Session getMailSession = Session.getDefaultInstance(
				config.mailConfig, null);
		MimeMessage generateMailMessage = new MimeMessage(getMailSession);
		try
		{
			/* create email */
			generateMailMessage.addRecipient(Message.RecipientType.TO,
					new InternetAddress(recipient));
			generateMailMessage.setSubject(emailSubject);
			generateMailMessage.setContent(emailBody, bodyFormat);
			
			/* login to server email account and send email. */
			Transport transport = getMailSession.getTransport();
			transport.connect(config.serverEmail, config.serverPassword);
			transport.sendMessage(generateMailMessage,
					generateMailMessage.getAllRecipients());
			transport.close();
		} catch (MessagingException me)
		{
			logger.log("Could not send email", me);
			return false;
		}
		return true;
	}
	
	private static class JSONMapData
	{
		JSONObject jobj;
		Map<String, String> jmap;
		
		@SuppressWarnings("unchecked")
		JSONMapData(JSONObject jobj)
		{
			this.jobj = jobj != null ? jobj : new JSONObject();
			this.jmap = (Map<String, String>) this.jobj;
		}
	}
	
	private static class JSONArrData
	{
		JSONArray jarr;
		List<String> jlist;
		
		@SuppressWarnings("unchecked")
		JSONArrData(JSONArray jarr)
		{
			this.jarr = jarr != null ? jarr : new JSONArray();
			this.jlist = (List<String>) this.jarr;
		}
	}

	/**
	 * This class contains the configuration data for sending emails.
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private static final class EmailConfig
	{
		static final String CONFIG_FILE =
				"servlet/implementation/email_settings.txt";
		static final String ACCOUNT_FILE =
				"servlet/implementation/email_accounts.ini";
		Properties mailConfig;
		
		// server mailing account
		String serverEmail, serverPassword, adminEmail;
		
		EmailConfig() throws IOException
		{
			mailConfig = new Properties();
			refreshConfig();
		}
		
		/**
		 * reloads the javax.mail config properties as well as
		 * the email account config.
		 */
		synchronized void refreshConfig() throws IOException
		{
			loadConfig(CONFIG_FILE);
			loadEmailAccounts(ACCOUNT_FILE);
		}
		
		/**
		 * Loads the javax.mail config properties contained in the
		 * supplied config file.
		 * 
		 * @param filePath The file while the javax.mail config
		 * 		properties are located.
		 * 
		 * @return True if the file was loaded. False if an error
		 * 		occurred.
		 */
		synchronized void loadConfig(String filePath) throws IOException
		{
			if (!mailConfig.isEmpty())
				mailConfig.clear();
			mailConfig.load(Utilities.getResourceStream(getClass(), filePath));
		}
		
		/**
		 * Loads the registration program's email account information
		 * as well as the email address of the administrator who will
		 * receive registration requests.
		 * 
		 * @param filePath The file that contains the email account
		 * 		information.
		 * 
		 * @return True if the file was loaded. False if an error
		 * 		occurred.
		 */
		synchronized void loadEmailAccounts(String filePath) throws IOException
		{
			Properties props = new Properties();
			props.load(Utilities.getResourceStream(getClass(), filePath));
			adminEmail = props.getProperty("admin_email");
			serverEmail = props.getProperty("server_email");
			serverPassword = props.getProperty("server_password");
			props.clear();
		}
	}
}
