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
package servlet.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import common.implementation.Constants;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Implementations;
import servlet.core.interfaces.Database.DatabaseFunction;
import servlet.implementation.exceptions.DBWriteException;

/**
 * This class handles redirecting a request from the applet to the
 * appropriate method in the servlet.
 * 
 * @author Marcus Malmquist
 *
 */
public class PPC
{
	private static JSONParser parser;
	private static Map<String, DatabaseFunction> dbm;
	private static PPCLogger logger;
	private static Database db;
	
	static {
		parser = new JSONParser();
		dbm = new HashMap<String, DatabaseFunction>();
		logger = PPCLogger.getLogger();
		db = Implementations.Database();

		dbm.put(ServletConst.CMD_ADD_USER, PPC::addUser);
		dbm.put(Constants.CMD_ADD_QANS, PPC::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, PPC::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, PPC::getClinics);
		dbm.put(Constants.CMD_GET_USER, PPC::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, PPC::setPassword);
		dbm.put(Constants.CMD_GET_ERR_MSG, PPC::getErrorMessages);
		dbm.put(Constants.CMD_GET_INFO_MSG, PPC::getInfoMessages);
		dbm.put(Constants.CMD_LOAD_Q, PPC::loadQuestions);
		dbm.put(Constants.CMD_LOAD_QR_DATE, PPC::loadQResultDates);
		dbm.put(Constants.CMD_LOAD_QR, PPC::loadQResults);
		dbm.put(Constants.CMD_REQ_REGISTR, PPC::requestRegistration);
		dbm.put(ServletConst.CMD_RSP_REGISTR, PPC::respondRegistration);
		dbm.put(Constants.CMD_REQ_LOGIN, PPC::requestLogin);
		dbm.put(Constants.CMD_REQ_LOGOUT, PPC::requestLogout);
	}
	
	/**
	 * Parses the {@code message} and redirects the request to the request
	 * to the appropriate method.
	 * 
	 * @param message The request from the applet.
	 * 
	 * @return The response from the servlet.
	 */
	public static String handleRequest(String message)
	{
		try {
			JSONData msg = new JSONData((JSONObject) parser.parse(message));
			return getDBMethod(msg.jmap.get("command")).dbfunc(msg.jobj);
		} catch (ParseException pe) {
			logger.log("Unknown JSON format", pe);
		} catch (Exception e) {
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
		} catch (ParseException pe) {
			throw new NullPointerException("JSON parse error");
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
		} catch (ParseException pe) {
			throw new NullPointerException("JSON parse error");
		}
	}

	private static String addUser(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_USER);
		try {
			db.addUser(Integer.parseInt(in.jmap.get("clinic_id")),
					in.jmap.get("name"), in.jmap.get("password"),
					in.jmap.get("email"), in.jmap.get("salt"));
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		}
		catch (DBWriteException dbw) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Database write error", dbw);
		}
		return out.jobj.toString();
	}

	private static String addQuestionnaireAnswers(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_ADD_QANS);
		
		int clinic_id = Integer.parseInt(in.jmap.get("clinic_id"));
		String identifier = in.jmap.get("identifier");

		JSONData m = new JSONData(getJSONObject(in.jmap.get("questions")));
		List<String> question_ids = new ArrayList<String>();
		List<String> question_answers = new ArrayList<String>();
		for (Entry<String, String> e : m.jmap.entrySet()) {
			question_ids.add((String) e.getKey());
			question_answers.add((String) e.getValue());
		}
		
		if (db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier,
						question_ids, question_answers)) {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		} else {
			out.jmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out.jobj.toString();
	}

	private static String addClinic(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
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
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_GET_CLINICS);
		
		Map<Integer, String> clinics = db.getClinics();
		if (clinics != null) {
			JSONData _jclin = new JSONData(null);
			for (Entry<Integer, String> e : clinics.entrySet()) {
				_jclin.jmap.put(Integer.toString(e.getKey()), e.getValue());
			}
			out.jmap.put("clinics", _jclin.jobj.toString());
		} else {
			out.jmap.put("clinics", (new JSONObject()).toString());
		}
		return out.jobj.toString();
	}

	private static String getUser(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_GET_USER);

		String username = in.jmap.get("name");
		_User _user = db._getUser(username);
		if (_user != null) {
			JSONData user = new JSONData(null);
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
		JSONData in = new JSONData(obj);
		JSONData err = new JSONData(null);
		err.jmap.put("command", Constants.CMD_GET_ERR_MSG);

		Map<String, String> userobj;
		try {
			userobj = getUser(in.jmap.get("name"));
		} catch (Exception e) {
			throw new NullPointerException("Can only update password for existing users");
		}

		JSONData user = new JSONData(getJSONObject(userobj.get("user")));
		
		String oldPass = in.jmap.get("old_password");
		String newPass = in.jmap.get("new_password");
		String newSalt = in.jmap.get("new_salt");

		if (!user.jmap.get("password").equals(oldPass)) {
			err.jmap.put("user", (new JSONObject()).toString());
			return err.jobj.toString();
		}
		
		if (db.setPassword(newPass, newSalt, user.jmap.get("name"))) {
			JSONData ret = new JSONData(null);
			ret.jmap.put("command", "get_user");
			ret.jmap.put("name", in.jmap.get("name"));
			return getUser(ret.jobj);
		} else {
			err.jmap.put("user", (new JSONObject()).toString());
			return err.jobj.toString();
		}
	}
	
	private static String getErrorMessages(JSONObject obj)
	{
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_GET_ERR_MSG);
		
		Map<String, _Message> errMsg = db.getErrorMessages();
		getMessages(out.jmap, errMsg);
		return out.jobj.toString();
	}
	
	private static String getInfoMessages(JSONObject obj)
	{
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_GET_INFO_MSG);
		
		Map<String, _Message> infoMsg = db.getInfoMessages();
		getMessages(out.jmap, infoMsg);
		return out.jobj.toString();
	}
	
	private static String loadQuestions(JSONObject obj)
	{
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_LOAD_Q);

		JSONData questions = new JSONData(null);
		Map<Integer, _Question> _questions = db.loadQuestions();

		for (Entry<Integer, _Question> e : _questions.entrySet()) {
			_Question q = e.getValue();
			JSONData question = new JSONData(null);
			int i = 0;
			for (String str : q.options) {
				question.jmap.put(String.format("option%d", i++), str);
			}
			String id = Integer.toString(q.id);
			question.jmap.put("type", q.type);
			question.jmap.put("id", id);
			question.jmap.put("question", q.question);
			question.jmap.put("description", q.description);
			question.jmap.put("optional", q.optional ? "1" : "0");
			question.jmap.put("max_val", Integer.toString(q.max_val));
			question.jmap.put("min_val", Integer.toString(q.min_val));

			questions.jmap.put(Integer.toString(e.getKey()),
					question.jobj.toString());
		}
		out.jmap.put("questions", questions.jobj.toString());
		return out.jobj.toString();
	}
	
	private static String loadQResultDates(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR_DATE);

		Map<String, String> _user;
		try {
			_user = getUser(in.jmap.get("name"));
		} catch (Exception e) {
			out.jmap.put("dates", (new JSONArray()).toString());
			logger.log("No user specified");
			return out.jobj.toString();
		}
		JSONData user = new JSONData(getJSONObject(_user.get("user")));

		JSONArrayData dates = new JSONArrayData(null);
		List<String> _dates = db.loadQResultDates(Integer.parseInt(user.jmap.get("clinic_id")));
		for (String str : _dates) {
			dates.jlist.add(str);
		}
		out.jmap.put("dates", dates.jarr.toString());
		return out.jobj.toString();
	}

	private static String loadQResults(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_LOAD_QR);

		Map<String, String> userobj;
		try {
			userobj = getUser(in.jmap.get("name"));
		} catch (Exception e) {
			out.jmap.put("dates", (new JSONObject()).toString());
			logger.log("No user specified");
			return out.jobj.toString();
		}
		JSONData user = new JSONData(getJSONObject(userobj.get("user")));

		JSONParser parser = new JSONParser();
		JSONArrayData _questions;
		try {
			_questions = new JSONArrayData((JSONArray) parser.parse(in.jmap.get("questions")));
		} catch (ParseException pe) {
			out.jmap.put("results", (new JSONObject()).toString());
			logger.log("Error parsing JSON object", pe);
			return out.jobj.toString();
		} catch (NullPointerException e) {
			out.jmap.put("results", (new JSONObject()).toString());
			logger.log("Missing 'questions' entry", e);
			return out.jobj.toString();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date begin = null;
		try {
			begin = sdf.parse(in.jmap.get("begin"));
		} catch (java.text.ParseException e) {
			out.jmap.put("results", (new JSONObject()).toString());
			logger.log("Malformed 'begin' date", e);
			return out.jobj.toString();
		}
		Date end = null;
		try {
			end = sdf.parse(in.jmap.get("end"));
		} catch (java.text.ParseException e) {
			out.jmap.put("results", (new JSONObject()).toString());
			logger.log("Malformed 'end' date", e);
			return out.jobj.toString();
		}

		List<Map<String, String>> _results = db.loadQResults(
				_questions.jlist,
				Integer.parseInt(user.jmap.get("clinic_id")), begin, end);

		JSONArrayData results = new JSONArrayData(null);
		for (Map<String, String> rmap : _results) {
			JSONData answers = new JSONData(null);
			for (Entry<String, String> e : rmap.entrySet()) {
				answers.jmap.put(e.getKey(), e.getValue());
			}
			results.jlist.add(answers.jobj.toString());
		}
		out.jmap.put("results", results.jarr.toString());
		return out.jobj.toString();
	}
	
	private static String requestRegistration(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_REQ_REGISTR);
		
		String name = in.jmap.get("name");
		String email = in.jmap.get("email");
		String clinic = in.jmap.get("clinic");
		boolean success = db.requestRegistration(name, email, clinic);
		
		out.jmap.put(Constants.INSERT_RESULT,
				success ? Constants.INSERT_SUCCESS : Constants.INSERT_FAIL);
		return out.jobj.toString();
	}
	
	private static String respondRegistration(JSONObject obj)
	{
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", ServletConst.CMD_RSP_REGISTR);
		
		String username = in.jmap.get("username");
		String email = in.jmap.get("email");
		String password = in.jmap.get("password");
		boolean success = db.respondRegistration(username, email, password);
		
		out.jmap.put(Constants.INSERT_RESULT,
				success ? Constants.INSERT_SUCCESS : Constants.INSERT_FAIL);
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
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGIN);

		Map<String, String> userobj;
		try {
			userobj = getUser(in.jmap.get("name"));
		} catch (Exception e) {
			out.jmap.put(Constants.LOGIN_REPONSE, Constants.INVALID_DETAILS_STR);
			return out.jobj.toString();
		}
		
		JSONData _user = new JSONData(getJSONObject(userobj.get("user")));

		if (!_user.jmap.get("password").equals(in.jmap.get("password"))) {
			out.jmap.put(Constants.LOGIN_REPONSE, Constants.INVALID_DETAILS_STR);
			return out.jobj.toString();
		}
		
		UserManager um = UserManager.getUserManager();
		out.jmap.put(Constants.LOGIN_REPONSE, um.addUser(_user.jmap.get("name")));
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
		JSONData in = new JSONData(obj);
		JSONData out = new JSONData(null);
		out.jmap.put("command", Constants.CMD_REQ_LOGOUT);

		UserManager um = UserManager.getUserManager();
		String response = um.delUser(in.jmap.get("name")) ? Constants.SUCCESS_STR : Constants.ERROR_STR;
		out.jmap.put(Constants.LOGOUT_REPONSE, response);
		return out.jobj.toString();
	}
	
	/**
	 * Quick method for calling {@code getUser(JSONObject)} using only the
	 * username as an argument.
	 * 
	 * @param username The username of the user to look for.
	 * 
	 * @return A map containing the information about the user.
	 * 
	 * @throws Exception If a parse error occurs.
	 */
	private static Map<String, String> getUser(String username) throws Exception
	{
		JSONData in = new JSONData(null);
		in.jmap.put("command", "get_user");
		in.jmap.put("name", username);

		JSONData out = new JSONData((JSONObject) parser.parse(getUser(in.jobj)));
		return out.jmap;
	}
	
	private static boolean getMessages(Map<String, String> retobj, Map<String, _Message> _messages)
	{
		JSONData messages = new JSONData(null);
		for (Entry<String, _Message> e : _messages.entrySet()) {
			_Message _msg = e.getValue();
			
			JSONData msg = new JSONData(null);
			for (Entry<String, String> _e : _msg.msg.entrySet()) {
				msg.jmap.put(_e.getValue(), _e.getKey());
			}

			JSONData message = new JSONData(null);
			message.jmap.put("name", _msg.name);
			message.jmap.put("code", _msg.code);
			message.jmap.put("message", msg.jobj.toString());

			messages.jmap.put(e.getKey(), message.jobj.toString());
		}
		retobj.put("messages", messages.jobj.toString());
		return true;
	}
	
	private static class JSONData {
		JSONObject jobj;
		Map<String, String> jmap;
		
		@SuppressWarnings("unchecked")
		JSONData(JSONObject jobj) {
			this.jobj = jobj != null ? jobj : new JSONObject();
			jmap = (Map<String, String>) this.jobj;
		}
	}
	
	private static class JSONArrayData {
		JSONArray jarr;
		List<String> jlist;
		
		@SuppressWarnings("unchecked")
		JSONArrayData(JSONArray jarr) {
			this.jarr = jarr != null ? jarr : new JSONArray();
			jlist = (List<String>) this.jarr;
		}
	}
}
