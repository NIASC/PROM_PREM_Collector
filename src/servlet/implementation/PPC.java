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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import common.implementation.Constants;
import servlet.core.Crypto;
import servlet.core.MailMan;
import servlet.core.PPCLogger;
import servlet.core.PasswordHandle;
import servlet.core.ServletConst;
import servlet.core.User;
import servlet.core.UserManager;
import servlet.core._Message;
import servlet.core._Question;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;

/**
 * This class handles redirecting a request from the applet to the
 * appropriate method in the servlet.
 * 
 * @author Marcus Malmquist
 *
 */
public class PPC
{
	
	/**
	 * Parses the {@code message} and redirects the request to the request
	 * to the appropriate method.
	 * 
	 * @param message The request from the applet.
	 * 
	 * @return The response from the servlet.
	 */
	public String handleRequest(String message, String remoteAddr, String hostAddr)
	{
		try {
			JSONMapData obj = new JSONMapData(getJSONObject(message));
			return getDBMethod(obj.jmap.get("command")).netfunc(obj.jobj, remoteAddr, hostAddr);
		} catch (Exception e) {
			logger.log("Unknown request", e);
			return null;
		}
	}
	
	PPC()
	{
		dbm = new HashMap<String, NetworkFunction>();
		db = MySQL_Database.getDatabase();
		um = UserManager.getUserManager();
		crypto = new SHA_Encryption();

		dbm.put(ServletConst.CMD_ADD_USER, this::addUser);
		dbm.put(Constants.CMD_ADD_QANS, this::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, this::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, this::getClinics);
		dbm.put(Constants.CMD_GET_USER, this::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, this::setPassword);
		dbm.put(Constants.CMD_GET_ERR_MSG, this::getErrorMessages);
		dbm.put(Constants.CMD_GET_INFO_MSG, this::getInfoMessages);
		dbm.put(Constants.CMD_LOAD_Q, this::loadQuestions);
		dbm.put(Constants.CMD_LOAD_QR_DATE, this::loadQResultDates);
		dbm.put(Constants.CMD_LOAD_QR, this::loadQResults);
		dbm.put(Constants.CMD_REQ_REGISTR, this::requestRegistration);
		dbm.put(ServletConst.CMD_RSP_REGISTR, this::respondRegistration);
		dbm.put(Constants.CMD_REQ_LOGIN, this::requestLogin);
		dbm.put(Constants.CMD_REQ_LOGOUT, this::requestLogout);
	}
	
	void terminate()
	{
		um.terminate();
	}
	
	private static PPCLogger logger;
	private static JSONParser parser;
	private Map<String, NetworkFunction> dbm;
	private Database db;
	private UserManager um;
	private Encryption crypto;
	
	static {
		logger = PPCLogger.getLogger();
		parser = new JSONParser();
	}


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
	
	private static Date getDate(String date)
	{
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0L);
		}
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
	private NetworkFunction getDBMethod(String command)
	{
		return dbm.get(command);
	}
	
	private String addUser(JSONObject obj, String remoteAddr, String hostAddr) throws Exception
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_USER);
		
		boolean success = remoteAddr.equals(hostAddr) && db.addUser(
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
	
	private String addQuestionnaireAnswers(
			JSONObject obj, String remoteAddr, String hostAddr)
					throws NullPointerException
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_ADD_QANS);
		
		try {
			if (storeQestionnaireAnswers(in))
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (NullPointerException | NumberFormatException e) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}
	
	private String addClinic(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_CLINIC);
		
		if (remoteAddr.equals(hostAddr) && db.addClinic(in.jmap.get("name"))) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		} else {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}
	
	private String getClinics(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_CLINICS);
			
		Map<Integer, String> _clinics = db.getClinics();
		JSONMapData clinics = new JSONMapData(null);
		if (remoteAddr.equals(hostAddr))
			for (Entry<Integer, String> e : _clinics.entrySet())
				clinics.jmap.put(Integer.toString(e.getKey()), e.getValue());
		out.jmap.put("clinics", clinics.jobj.toString());
		return out.jobj.toString();
	}
	
	private String getUser(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_USER);

		User _user = db.getUser(in.jmap.get("name"));
		JSONMapData user = new JSONMapData(null);
		if (remoteAddr.equals(hostAddr) && _user != null) {
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
	
	private String setPassword(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_SET_PASSWORD);
		try {
			int status = storePassword(in);
			out.jmap.put(Constants.SETPASS_REPONSE,
					Integer.toString(status));
		} catch (NullPointerException | NumberFormatException e) {
			out.jmap.put(Constants.SETPASS_REPONSE,
					Integer.toString(Constants.ERROR));
		}
		return out.jobj.toString();
	}
	
	@Deprecated
	private String getErrorMessages(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_ERR_MSG);
		out.jmap.put("messages", getMessages(db.getErrorMessages()).toString());
		return out.jobj.toString();
	}

	@Deprecated
	private String getInfoMessages(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_INFO_MSG);
		out.jmap.put("messages", getMessages(db.getInfoMessages()).toString());
		return out.jobj.toString();
	}
	
	private String loadQuestions(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_Q);
		
		try {
			out.jmap.put("questions", retrieveQuestions().jobj.toString());
		} catch (NullPointerException | NumberFormatException e) {
			out.jmap.put("questions", new JSONMapData(null).jobj.toString());
		}
		return out.jobj.toString();
	}
	
	private String loadQResultDates(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR_DATE);

		try {
			out.jmap.put("dates", retrieveQResultDates(in).jarr.toString());
		} catch (NullPointerException | NumberFormatException e) {
			out.jmap.put("dates", new JSONArrData(null).jarr.toString());
		}
		return out.jobj.toString();
	}
	
	private String loadQResults(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR);
		
		try {
			out.jmap.put("results", retrieveQResults(in).jarr.toString());
		} catch (Exception e) {
			out.jmap.put("results", new JSONArrData(null).jarr.toString());
		}
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
	private String requestRegistration(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_REGISTR);

		try {
			if (sendRegistration(in))
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
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
	private String respondRegistration(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_RSP_REGISTR);
		
		try {
			if (sendRegResp(in))
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
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
	private String requestLogin(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGIN);

		try {
			UserLogin ret = login(in);
			out.jmap.put(Constants.LOGIN_REPONSE, Integer.toString(ret.response));
			out.jmap.put("update_password", ret.user.update_password ? "1" : "0");
			if (ret.response == Constants.SUCCESS)
				out.jmap.put(Constants.LOGIN_UID, Long.toString(ret.uid));
			else
				out.jmap.put(Constants.LOGIN_UID, Long.toString(0L));
		} catch (Exception e) {
			out.jmap.put("update_password", "0");
			out.jmap.put(Constants.LOGIN_REPONSE, Constants.INVALID_DETAILS_STR);
			out.jmap.put(Constants.LOGIN_UID, Long.toString(0L));
		}
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
	private String requestLogout(JSONObject obj, String remoteAddr, String hostAddr)
	{
		JSONMapData in = new JSONMapData(obj);
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGOUT);

		try {
			out.jmap.put(Constants.LOGOUT_REPONSE, Integer.toString(logout(in)));
		} catch (Exception e) {
			out.jmap.put(Constants.LOGOUT_REPONSE, Integer.toString(Constants.ERROR));
		}
		return out.jobj.toString();
	}
	
	// --------------------------------

	@Deprecated
	private JSONObject getMessages(Map<String, _Message> _msg)
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
	
	private boolean storeQestionnaireAnswers(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		String _details = in.jmap.get("details");
		String _patient = in.jmap.get("patient");
		String _questions = in.jmap.get("questions");

		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(_details)));
		JSONMapData patient = new JSONMapData(getJSONObject(Crypto.decrypt(_patient)));
		
		long uid = Long.parseLong(inpl.jmap.get("uid"));
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		String identifier = Implementations.Encryption().encryptMessage(
				patient.jmap.get("forename"),
				patient.jmap.get("personal_id"),
				patient.jmap.get("surname"));

		List<String> answers = new ArrayList<String>();
		JSONArrData m = new JSONArrData(getJSONArray(_questions));
		for (String str : m.jlist)
			answers.add(QDBFormat.getDBFormat(new JSONMapData(getJSONObject(str))));
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
	
	private int storePassword(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		long uid = Long.parseLong(inpl.jmap.get("uid"));
		String name = um.nameForUID(uid);
		String oldPass = inpl.jmap.get("old_password");
		String newPass1 = inpl.jmap.get("new_password1");
		String newPass2 = inpl.jmap.get("new_password2");

		Encryption hash = Implementations.Encryption();
		String newSalt = hash.getNewSalt();
		
		User user = db.getUser(name);
		int status = PasswordHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (status == Constants.SUCCESS) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					hash.hashString(newPass1, newSalt), newSalt);
		}
		return status;
	}
	
	private JSONMapData retrieveQuestions()
			throws NullPointerException, NumberFormatException
	{
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
		return _questions;
	}
	
	private JSONArrData retrieveQResultDates(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		long uid = Long.parseLong(inpl.jmap.get("uid"));
		User user = db.getUser(um.nameForUID(uid));
		List<String> dlist = db.loadQResultDates(user.clinic_id);

		JSONArrData dates = new JSONArrData(null);
		for (String str : dlist)
			dates.jlist.add(str);
		return dates;
	}
	
	private JSONArrData retrieveQResults(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		long uid = Long.parseLong(inpl.jmap.get("uid"));
		User _user = db.getUser(um.nameForUID(uid));
		JSONArrData questions = new JSONArrData(getJSONArray(in.jmap.get("questions")));
		List<Integer> qlist = new ArrayList<Integer>();
		for (String str : questions.jlist)
			qlist.add(Integer.parseInt(str));

		List<Map<String, String>> _results = db.loadQResults(
				_user.clinic_id, qlist,
				getDate(in.jmap.get("begin")),
				getDate(in.jmap.get("end")));

		JSONArrData results = new JSONArrData(null);
		for (Map<String, String> m : _results) {
			JSONMapData answers = new JSONMapData(null);
			for (Entry<String, String> e : m.entrySet())
				answers.jmap.put(e.getKey().substring("question".length()),
						QDBFormat.getQFormat(e.getValue()));
			results.jlist.add(answers.jobj.toString());
		}
		return results;
	}
	
	private boolean sendRegistration(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		String name = inpl.jmap.get("name");
		String email = inpl.jmap.get("email");
		String clinic = inpl.jmap.get("clinic");
		
		return MailMan.sendRegReq(name, email, clinic);
	}
	
	private boolean sendRegResp(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		String username = in.jmap.get("username");
		String password = in.jmap.get("password");
		String email = in.jmap.get("password");
		return MailMan.sendRegResp(username, password, email);
	}
	
	private UserLogin login(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		UserLogin ret = new UserLogin();
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		ret.user = db.getUser(inpl.jmap.get("name"));
		if (!ret.user.passwordMatch(inpl.jmap.get("password")))
			throw new NullPointerException("invalid details");

		String hash = crypto.encryptMessage(
				Long.toHexString((new Date()).getTime()),
				ret.user.name, crypto.getNewSalt());
		ret.uid = Long.parseLong(hash.substring(0, 2*Long.BYTES-1), 2*Long.BYTES);
		
		ret.response = um.addUser(ret.user.name, ret.uid);
		return ret;
	}
	
	private int logout(JSONMapData in)
			throws NullPointerException, NumberFormatException
	{
		JSONMapData inpl = new JSONMapData(getJSONObject(Crypto.decrypt(in.jmap.get("details"))));
		long uid = Long.parseLong(inpl.jmap.get("uid"));
		return um.delUser(um.nameForUID(uid)) ? Constants.SUCCESS : Constants.ERROR;
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

	@FunctionalInterface
	private interface NetworkFunction
	{
		/**
		 * A method that processes the request contained in {@code obj}
		 * and returns the answer as a string.
		 * 
		 * @param obj The JSONObject that contains the request along with
		 * 		required data to process the request.
		 * 
		 * @return The String representation of the JSONObject that
		 * 		contains the answer.
		 */
		public String netfunc(JSONObject obj, String remoteAddr, String hostAddr) throws Exception;
	}

	private static class QDBFormat
	{
		static String getDBFormat(JSONMapData fc)
		{
			String val = null;
			if ((val = fc.jmap.get("SingleOption")) != null) {
				
				return String.format("'option%d'", Integer.parseInt(val));
				
			} else if ((val = fc.jmap.get("MultipleOption")) != null) {
				
				JSONArrData options = new JSONArrData(getJSONArray(val));
				List<String> lstr = new ArrayList<>();
				for (String str : options.jlist)
					lstr.add(String.format("option%d", Integer.parseInt(str)));
				return String.format("[%s]", String.join(",", lstr));
				
			} else if ((val = fc.jmap.get("Slider")) != null) {
				
				return String.format("'slider%d'", Integer.parseInt(val));
				
			} else if ((val = fc.jmap.get("Area")) != null) {
				
				return String.format("'%s'", val);
				
			} else
				
				return "''";
		}
		
		static String getQFormat(String dbEntry)
		{
			JSONMapData fmt = new JSONMapData(null);
			if (dbEntry == null || dbEntry.trim().isEmpty())
				return fmt.jobj.toString();
			
			if (dbEntry.startsWith("option")) {
				fmt.jmap.put("SingleOption", dbEntry.substring("option".length()));
			} else if (dbEntry.startsWith("slider")) {
				fmt.jmap.put("Slider", dbEntry.substring("slider".length()));
			} else if (dbEntry.startsWith("[") && dbEntry.endsWith("]")) {
                /* multiple answers */
				List<String> entries = Arrays.asList(dbEntry.split(","));
				JSONArrData options = new JSONArrData(null);
				if (entries.get(0).startsWith("option")) {
                    /* multiple option */
					for (String str : entries)
						options.jlist.add(str.substring("option".length()));
					fmt.jmap.put("MultipleOption", options.jarr.toString());
				}
			} else {
                /* must be plain text entry */
				fmt.jmap.put("Area", dbEntry);
			}
			return fmt.jobj.toString();
		}
	}
	
	private class UserLogin
	{
		User user;
		long uid;
		int response;
	}
}
