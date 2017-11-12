/** ServletCommunication.java
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
package servlet.manage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import common.implementation.Constants;
import servlet.core.ServletConst;
import servlet.core._User;

/**
 * This class is an example of an implementation of
 * Database_Interface. This is done using a MySQL database and a
 * MySQL Connector/J to provide a MySQL interface to Java.
 * 
 * This class is designed to be thread safe and a singleton.
 * 
 * @author Marcus Malmquist
 *
 */
public class ServletCommunication
{
	/* Public */
	
	/**
	 * Retrieves the active instance of this class.
	 * 
	 * @return The active instance of this class.
	 */
	public static synchronized ServletCommunication getInstance()
	{
		if (database == null)
			database = new ServletCommunication();
		return database;
	}

	@Override
	public final Object clone()
			throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	
	/**
	 * Adds a new user to the database.
	 * 
	 * @param username The username of the new user.
	 * @param password The (hashed) password of the new user.
	 * @param salt The salt that was used to hash the password.
	 * @param clinic The clinic ID that the new user belongs to.
	 * @param email The email of the new user.
	 * 
	 * @return {@code true} on successful update,
	 *		{@code false} on failure.
	 */
	public boolean addUser(String username, String password,
			String salt, int clinic, String email)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_USER);
		out.jmap.put("clinic_id", Integer.toString(clinic));
		out.jmap.put("name", username);
		out.jmap.put("password", password);
		out.jmap.put("email", email);
		out.jmap.put("salt", salt);

		JSONMapData _ans = new JSONMapData(sendMessage(out.jobj));
		String insert = _ans.jmap.get(Constants.INSERT_RESULT);
		return (insert != null && insert.equals(Constants.INSERT_SUCCESS));
	}

	/**
	 * Adds a new clinic to the database.
	 * 
	 * @param clinicName The name of the clinic.
	 * 
	 * @return {@code true} on successful update,
	 *		{@code false} on failure.
	 */
	public boolean addClinic(String clinicName)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_ADD_CLINIC);
		out.jmap.put("name", clinicName);

		JSONMapData _ans = new JSONMapData(sendMessage(out.jobj));
		String insert = _ans.jmap.get(Constants.INSERT_RESULT);
		return (insert != null && insert.equals(Constants.INSERT_SUCCESS));
	}
	
	/**
	 * Collects the clinic names and id and places them in a Map.
	 * 
	 * @return A Map containing clinic id as keys and clinic names
	 * 		as values.
	 */
	public Map<Integer, String> getClinics()
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_CLINICS);

		JSONMapData _ans = new JSONMapData(sendMessage(out.jobj));
		JSONMapData _clinics = new JSONMapData(getJSONObject(_ans.jmap.get("clinics")));
		
		Map<Integer, String> clinic = new TreeMap<Integer, String>();
		for (Entry<String, String> e : _clinics.jmap.entrySet())
			clinic.put(Integer.parseInt(e.getKey()), e.getValue());
		return clinic;
	}
	
	/**
	 * Collects the information about the user from the database.
	 * 
	 * @param username The name of the user to look for.
	 * 
	 * @return If the user was found the instance of the user is
	 * 		returned else {@code null}.
	 */
	public _User getUser(String username)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", Constants.CMD_GET_USER);
		out.jmap.put("name", username);

		JSONMapData _ans = new JSONMapData(sendMessage(out.jobj));
		JSONMapData _user = new JSONMapData(getJSONObject(_ans.jmap.get("user")));
		try {
			_User _usr = new _User();
			_usr.clinic_id = Integer.parseInt(_user.jmap.get("clinic_id"));
			_usr.name = _user.jmap.get("name");
			_usr.password = _user.jmap.get("password");
			_usr.email = _user.jmap.get("email");
			_usr.salt = _user.jmap.get("salt");
			_usr.update_password = Integer.parseInt(_user.jmap.get("update_password")) > 0;
			return _usr;
		} catch (NullPointerException | NumberFormatException _e) {
			return null;
		}
	}
	
	/**
	 * Sends a response to the registration request.
	 * 
	 * @param username The username of the added user.
	 * @param password The unhashed password of the added user.
	 * @param email The email of the person who requested registration.
	 * 
	 * @return {@code true} if the response was successfully sent,
	 *		{@code false} on failure.
	 */
	public boolean respondRegistration(
			String username, String password, String email)
	{
		JSONMapData out = new JSONMapData(null);
		out.jmap.put("command", ServletConst.CMD_RSP_REGISTR);
		out.jmap.put("username", username);
		out.jmap.put("password", password);
		out.jmap.put("email", email);

		JSONMapData _ans = new JSONMapData(sendMessage(out.jobj));
		String insert = _ans.jmap.get(Constants.INSERT_RESULT);
		return (insert != null && insert.equals(Constants.INSERT_SUCCESS));
	}
	
	/* Protected */
	
	/* Private */

	private static ServletCommunication database;
	
	private JSONParser parser;
	
	/**
	 * Initializes variables and loads the database configuration.
	 * This class is a singleton and should only be instantiated once.
	 */
	private ServletCommunication()
	{
		parser = new JSONParser();
	}
	
	/**
	 * Sends a JSONObject to the servlet.
	 * 
	 * @param obj The JSONObject to send.
	 * 
	 * @return The JSONObject returned from the servlet.
	 */
	private JSONObject sendMessage(JSONObject obj)
	{
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) ServletConst.LOCAL_URL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			/* send message */
			OutputStream os = connection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			osw.write(obj.toString());
			osw.flush();
			osw.close();

			/* receive message */
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();
			return getJSONObject(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
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
	private JSONObject getJSONObject(String str)
	{
		try {
			return (JSONObject) parser.parse(str);
		} catch (Exception pe) {
			return null;
		}
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