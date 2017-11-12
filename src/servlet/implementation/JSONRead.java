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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import common.implementation.Constants;
import servlet.core.PPCLogger;
import servlet.core.ServletConst;
import servlet.core.UserManager;
import servlet.core._User;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Implementations;
import servlet.core.interfaces.Database.DatabaseFunction;
import servlet.implementation.exceptions.DBReadException;
import servlet.implementation.exceptions.DBWriteException;

/**
 * This class handles redirecting a request from the applet to the
 * appropriate method in the servlet.
 * 
 * @author Marcus Malmquist
 *
 */
public class JSONRead
{
	private static JSONParser parser;
	private static Map<String, DatabaseFunction> dbm;
	private static Database db;
	
	static {
		parser = new JSONParser();
		dbm = new HashMap<String, DatabaseFunction>();
		db = Implementations.Database();

		Database db = Implementations.Database();
		dbm.put(ServletConst.CMD_ADD_USER, JSONRead::addUser);
		dbm.put(Constants.CMD_ADD_QANS, JSONRead::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, JSONRead::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, JSONRead::getClinics);
		dbm.put(Constants.CMD_GET_USER, JSONRead::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, JSONRead::setPassword);
		dbm.put(Constants.CMD_GET_ERR_MSG, db::getErrorMessages);
		dbm.put(Constants.CMD_GET_INFO_MSG, db::getInfoMessages);
		dbm.put(Constants.CMD_LOAD_Q, db::loadQuestions);
		dbm.put(Constants.CMD_LOAD_QR_DATE, db::loadQResultDates);
		dbm.put(Constants.CMD_LOAD_QR, db::loadQResults);
		dbm.put(Constants.CMD_REQ_REGISTR, db::requestRegistration);
		dbm.put(ServletConst.CMD_RSP_REGISTR, db::respondRegistration);
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
	
	private static PPCLogger logger = PPCLogger.getLogger();
	
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

	private static JSONObject getJSONObject(String str)
	{
		try {
			return (JSONObject) parser.parse(str);
		} catch (ParseException pe) {
			throw new NullPointerException("JSON parse error");
		}
	}


	private static Map<String, String> getUser(String username) throws Exception
	{
		JSONMapData getuser = new JSONMapData(null);
		getuser.jmap.put("command", "get_user");
		getuser.jmap.put("name", username);
		
		JSONMapData out = new JSONMapData((JSONObject) parser.parse(getUser(getuser.jobj)));
		return out.jmap;
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
}
