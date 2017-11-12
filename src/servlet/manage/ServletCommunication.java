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
@SuppressWarnings("unchecked")
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
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_ADD_USER);
		rmap.put("clinic_id", Integer.toString(clinic));
		rmap.put("name", username);
		rmap.put("password", password);
		rmap.put("email", email);
		rmap.put("salt", salt);
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		String insert = (String) ans.get(Constants.INSERT_RESULT);
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
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_ADD_CLINIC);
		rmap.put("name", clinicName);
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		Map<String, String> amap = (Map<String, String>) ans;
		String insert = amap.get(Constants.INSERT_RESULT);
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
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_CLINICS);
		
		JSONObject ans = sendMessage(ret);
		JSONObject clinics = getJSONObject((String) ans.get("clinics"));
		Map<String, String> cmap = (Map<String, String>) clinics;
		
		Map<Integer, String> clinic = new TreeMap<Integer, String>();
		for (Entry<String, String> e : cmap.entrySet())
		{
			clinic.put(Integer.parseInt(e.getKey()),
					e.getValue());
		}
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
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_USER);
		rmap.put("name", username);

		JSONObject ans = sendMessage(ret);
		JSONObject user = getJSONObject((String) ans.get("user"));
		Map<String, String> umap = (Map<String, String>) user;
		_User _usr = null;
		try {
			_usr = new _User();
			_usr.clinic_id = Integer.parseInt(umap.get("clinic_id"));
			_usr.name = umap.get("name");
			_usr.password = umap.get("password");
			_usr.email = umap.get("email");
			_usr.salt = umap.get("salt");
			_usr.update_password = Integer.parseInt(umap.get("update_password")) > 0;
		}
		catch (NullPointerException _e) {}
		catch (NumberFormatException _e) {}
		return _usr;
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
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_RSP_REGISTR);
		rmap.put("username", username);
		rmap.put("password", password);
		rmap.put("email", email);
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		String insert = (String) ans.get(Constants.INSERT_RESULT);
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
			// System.out.println(obj);
			osw.write(obj.toString());
			osw.flush();
			osw.close();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// System.out.println("Ok response");
			} else {
				// System.out.println("Bad response");
			}

			/* receive message */
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();
			// System.out.printf(">>%s<<\n", sb.toString());
			return getJSONObject(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
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
		try
		{
			return (JSONObject) parser.parse(str);
		}
		catch (org.json.simple.parser.ParseException pe) { }
		return null;
	}
}