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
import java.util.Collections;
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
			MapData obj = new MapData(message);
			return getDBMethod(obj.get("command")).netfunc(obj, remoteAddr, hostAddr).toString();
		} catch (Exception e) {
			logger.log("Unknown request", e);
			return new MapData().toString();
		}
	}
	
	PPC()
	{
		dbm = new HashMap<String, NetworkFunction>();
		db = MySQL_Database.getDatabase();
		um = UserManager.getUserManager();
		crypto = new SHA_Encryption();
		qdbf = new QDBFormat();

		dbm.put(ServletConst.CMD_ADD_USER, this::addUser);
		dbm.put(Constants.CMD_ADD_QANS, this::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, this::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, this::getClinics);
		dbm.put(Constants.CMD_GET_USER, this::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, this::setPassword);
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
	private QDBFormat qdbf;
	
	static {
		logger = PPCLogger.getLogger();
		parser = new JSONParser();
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
	
	private MapData addUser(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", ServletConst.CMD_ADD_USER);

		try {
			if (remoteAddr.equals(hostAddr) && storeUser(in))
				out.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out;
	}
	
	private MapData addQuestionnaireAnswers(
			MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_ADD_QANS);
		
		try {
			if (storeQestionnaireAnswers(in))
				out.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out;
	}
	
	private MapData addClinic(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", ServletConst.CMD_ADD_CLINIC);
		
		try {
			if (remoteAddr.equals(hostAddr) && storeClinic(in))
				out.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out;
	}
	
	private MapData getClinics(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_GET_CLINICS);

		try {
			if (remoteAddr.equals(hostAddr))
				out.put("clinics", retrieveClinics().toString());
			else
				out.put("clinics", new MapData().toString());
		} catch (Exception e) {
			out.put("clinics", new MapData().toString());
		}
		return out;
	}
	
	private MapData getUser(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_GET_USER);

		try {
			if (remoteAddr.equals(hostAddr))
				out.put("user", retrieveUser(in).toString());
			else
				out.put("user", new MapData().toString());
		} catch (Exception e) {
			out.put("user", new MapData().toString());
		}
		return out;
	}
	
	private MapData setPassword(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_SET_PASSWORD);
		try {
			int status = storePassword(in);
			out.put(Constants.SETPASS_REPONSE,
					Integer.toString(status));
		} catch (Exception e) {
			out.put(Constants.SETPASS_REPONSE,
					Integer.toString(Constants.ERROR));
		}
		return out;
	}
	
	private MapData loadQuestions(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_LOAD_Q);
		
		try {
			out.put("questions", retrieveQuestions().toString());
		} catch (Exception e) {
			out.put("questions", new MapData().toString());
		}
		return out;
	}
	
	private MapData loadQResultDates(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_LOAD_QR_DATE);

		try {
			out.put("dates", retrieveQResultDates(in).toString());
		} catch (Exception e) {
			out.put("dates", new ListData().toString());
		}
		return out;
	}
	
	private MapData loadQResults(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_LOAD_QR);
		
		try {
			out.put("results", retrieveQResults(in).toString());
		} catch (Exception e) {
			out.put("results", new ListData().toString());
		}
		return out;
	}

	/**
	 * Sends a registration request to an administrator.
	 * 
	 * @param obj The JSONObject that contains the request, including
	 * 		the name, clinic and email.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private MapData requestRegistration(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_REQ_REGISTR);

		try {
			if (sendRegistration(in))
				out.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out;
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
	private MapData respondRegistration(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", ServletConst.CMD_RSP_REGISTR);
		
		try {
			if (sendRegResp(in))
				out.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
			else
				out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		} catch (Exception e) {
			out.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		}
		return out;
	}

	/**
	 * Requests to log in.
	 * 
	 * @param obj The JSONObject that contains the request, including the
	 * 		username.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private MapData requestLogin(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_REQ_LOGIN);

		try {
			UserLogin ret = login(in);
			out.put(Constants.LOGIN_REPONSE, Integer.toString(ret.response));
			out.put("update_password", ret.user.update_password ? "1" : "0");
			if (ret.response == Constants.SUCCESS)
				out.put(Constants.LOGIN_UID, Long.toString(ret.uid));
			else
				out.put(Constants.LOGIN_UID, Long.toString(0L));
		} catch (Exception e) {
			out.put("update_password", "0");
			out.put(Constants.LOGIN_REPONSE, Constants.INVALID_DETAILS_STR);
			out.put(Constants.LOGIN_UID, Long.toString(0L));
		}
		return out;
	}

	/**
	 * Requests to log out.
	 * 
	 * @param obj The JSONObject that contains the request, including the
	 * 		username.
	 * 
	 * @return A JSONObject that contains the status of the request.
	 */
	private MapData requestLogout(MapData in, String remoteAddr, String hostAddr)
	{
		MapData out = new MapData();
		out.put("command", Constants.CMD_REQ_LOGOUT);

		try {
			out.put(Constants.LOGOUT_REPONSE, Integer.toString(logout(in)));
		} catch (Exception e) {
			out.put(Constants.LOGOUT_REPONSE, Integer.toString(Constants.ERROR));
		}
		return out;
	}
	
	// --------------------------------
	
	private boolean storeUser(MapData in)
			throws NullPointerException, NumberFormatException,
			IllegalArgumentException
	{
		int clinic_id = Integer.parseInt(in.get("clinic_id"));
		String name = in.get("name");
		String password = in.get("password");
		String email = in.get("email");
		String salt = in.get("salt");
		return db.addUser(clinic_id, name, password, email, salt);
	}
	
	private boolean storeQestionnaireAnswers(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		MapData patient = new MapData(Crypto.decrypt(in.get("patient")));
		
		long uid = Long.parseLong(inpl.get("uid"));
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		String identifier = Implementations.Encryption().encryptMessage(
				patient.get("forename"),
				patient.get("personal_id"),
				patient.get("surname"));

		List<String> answers = new ArrayList<String>();
		ListData m = new ListData(in.get("questions"));
		for (String str : m.iterable())
			answers.add(qdbf.getDBFormat(new MapData(str)));
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
	
	private boolean storeClinic(MapData in)
			throws NullPointerException, NumberFormatException
	{
		String name = in.get("name");
		return db.addClinic(name);
	}
	
	private MapData retrieveClinics()
			throws NullPointerException, NumberFormatException
	{
		Map<Integer, String> _clinics = db.getClinics();
		MapData clinics = new MapData();
		for (Entry<Integer, String> e : _clinics.entrySet())
			clinics.put(Integer.toString(e.getKey()), e.getValue());
		return clinics;
	}
	
	private MapData retrieveUser(MapData in)
			throws NullPointerException, NumberFormatException
	{
		User _user = db.getUser(in.get("name"));
		MapData user = new MapData();
		user.put("clinic_id", Integer.toString(_user.clinic_id));
		user.put("name", _user.name);
		user.put("password", _user.password);
		user.put("email", _user.email);
		user.put("salt", _user.salt);
		user.put("update_password", _user.update_password ? "1" : "0");
		return user;
	}
	
	private int storePassword(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		long uid = Long.parseLong(inpl.get("uid"));
		String name = um.nameForUID(uid);
		String oldPass = inpl.get("old_password");
		String newPass1 = inpl.get("new_password1");
		String newPass2 = inpl.get("new_password2");

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
	
	private MapData retrieveQuestions()
			throws NullPointerException, NumberFormatException
	{
		Map<Integer, _Question> questions = db.loadQuestions();
		MapData _questions = new MapData();
		for (Entry<Integer, _Question> _e : questions.entrySet()) {
			_Question _q = _e.getValue();
			MapData _question = new MapData();
			int i = 0;
			for (String str : _q.options)
				_question.put(String.format("option%d", i++), str);
			_question.put("type", _q.type);
			_question.put("id", Integer.toString(_q.id));
			_question.put("question", _q.question);
			_question.put("description", _q.description);
			_question.put("optional", _q.optional ? "1" : "0");
			_question.put("max_val", Integer.toString(_q.max_val));
			_question.put("min_val", Integer.toString(_q.min_val));
			
			_questions.put(Integer.toString(_e.getKey()),
					_question.toString());
		}
		return _questions;
	}
	
	private ListData retrieveQResultDates(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		long uid = Long.parseLong(inpl.get("uid"));
		User user = db.getUser(um.nameForUID(uid));
		List<String> dlist = db.loadQResultDates(user.clinic_id);

		ListData dates = new ListData();
		for (String str : dlist)
			dates.add(str);
		return dates;
	}
	
	private ListData retrieveQResults(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		long uid = Long.parseLong(inpl.get("uid"));
		User _user = db.getUser(um.nameForUID(uid));
		ListData questions = new ListData(in.get("questions"));
		List<Integer> qlist = new ArrayList<Integer>();
		for (String str : questions.iterable())
			qlist.add(Integer.parseInt(str));

		List<Map<String, String>> _results = db.loadQResults(
				_user.clinic_id, qlist,
				getDate(in.get("begin")),
				getDate(in.get("end")));

		ListData results = new ListData();
		for (Map<String, String> m : _results) {
			MapData answers = new MapData();
			for (Entry<String, String> e : m.entrySet())
				answers.put(e.getKey().substring("question".length()),
						qdbf.getQFormat(e.getValue()));
			results.add(answers.toString());
		}
		return results;
	}
	
	private boolean sendRegistration(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		String name = inpl.get("name");
		String email = inpl.get("email");
		String clinic = inpl.get("clinic");
		
		return MailMan.sendRegReq(name, email, clinic);
	}
	
	private boolean sendRegResp(MapData in)
			throws NullPointerException, NumberFormatException
	{
		String username = in.get("username");
		String password = in.get("password");
		String email = in.get("email");
		return MailMan.sendRegResp(username, password, email);
	}
	
	private UserLogin login(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		UserLogin ret = new UserLogin();
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		ret.user = db.getUser(inpl.get("name"));
		if (!ret.user.passwordMatch(inpl.get("password")))
			throw new NullPointerException("invalid details");

		String hash = crypto.encryptMessage(
				Long.toHexString((new Date()).getTime()),
				ret.user.name, crypto.getNewSalt());
		ret.uid = Long.parseLong(hash.substring(0, 2*Long.BYTES-1), 2*Long.BYTES);
		
		ret.response = um.addUser(ret.user.name, ret.uid);
		return ret;
	}
	
	private int logout(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get("details")));
		long uid = Long.parseLong(inpl.get("uid"));
		return um.delUser(um.nameForUID(uid)) ? Constants.SUCCESS : Constants.ERROR;
	}
	
	private static class MapData
	{
		JSONObject jobj;
		Map<String, String> jmap;
		
		MapData()
		{
			this((JSONObject) null);
		}
		
		@SuppressWarnings("unchecked")
		MapData(JSONObject jobj)
		{
			this.jobj = jobj != null ? jobj : new JSONObject();
			this.jmap = (Map<String, String>) this.jobj;
		}
		
		MapData(String jsonString)
				throws org.json.simple.parser.ParseException,
				ClassCastException
		{
			this((JSONObject) parser.parse(jsonString));
		}
		
		void put(String key, String value)
		{
			jmap.put(key, value);
		}
		
		String get(String key)
		{
			return jmap.get(key);
		}
		
		Iterable<Entry<String, String>> iterable()
		{
			return jmap.entrySet();
		}
		
		@Override
		public String toString()
		{
			return jobj.toString();
		}
	}
	
	private static class ListData
	{
		JSONArray jarr;
		List<String> jlist;
		
		ListData()
		{
			this((JSONArray) null);
		}
		
		@SuppressWarnings("unchecked")
		ListData(JSONArray jarr)
		{
			this.jarr = jarr != null ? jarr : new JSONArray();
			this.jlist = (List<String>) this.jarr;
		}
		
		ListData(String jsonString)
				throws org.json.simple.parser.ParseException,
				ClassCastException
		{
			this((JSONArray) parser.parse(jsonString));
		}
		
		void add(String value)
		{
			jlist.add(value);
		}
		
		Iterable<String> iterable()
		{
			return Collections.unmodifiableList(jlist);
		}
		
		@Override
		public String toString()
		{
			return jarr.toString();
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
		public MapData netfunc(MapData in, String remoteAddr, String hostAddr) throws Exception;
	}

	private class QDBFormat
	{
		String getDBFormat(MapData fc)
				throws org.json.simple.parser.ParseException,
				ClassCastException,
				NumberFormatException
		{
			String val = null;
			if ((val = fc.get("SingleOption")) != null)
				return db.escapeReplace(String.format("option%d",
						Integer.parseInt(val)));
			else if ((val = fc.get("MultipleOption")) != null) {
				List<String> lstr = new ArrayList<>();
				for (String str : new ListData(val).iterable())
					lstr.add(String.format("option%d", Integer.parseInt(str)));
				return db.escapeReplace(lstr);
			} else if ((val = fc.get("Slider")) != null)
				return db.escapeReplace(String.format("slider%d",
						Integer.parseInt(val)));
			else if ((val = fc.get("Area")) != null)
				return db.escapeReplace(val);
			else
				return db.escapeReplace("");
		}
		
		String getQFormat(String dbEntry)
		{
			MapData fmt = new MapData();
			if (dbEntry == null || dbEntry.trim().isEmpty())
				return fmt.toString();
			
			if (dbEntry.startsWith("option")) {
				fmt.put("SingleOption", dbEntry.substring("option".length()));
			} else if (dbEntry.startsWith("slider")) {
				fmt.put("Slider", dbEntry.substring("slider".length()));
			} else if (db.isSQLList(dbEntry)) {
                /* multiple answers */
				List<String> entries = db.SQLListToJavaList(dbEntry);
				ListData options = new ListData();
				if (entries.get(0).startsWith("option")) {
                    /* multiple option */
					for (String str : entries)
						options.add(str.substring("option".length()));
					fmt.put("MultipleOption", options.toString());
				}
			} else {
                /* must be plain text entry */
				fmt.put("Area", dbEntry);
			}
			return fmt.toString();
		}
	}
	
	private class UserLogin
	{
		User user;
		long uid;
		int response;
	}
}
