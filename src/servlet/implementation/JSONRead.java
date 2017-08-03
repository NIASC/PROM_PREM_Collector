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

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import common.implementation.Constants;
import servlet.core.ServletConst;
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
	private static JSONParser parser;
	private static Map<String, DatabaseFunction> dbm;
	
	static {
		parser = new JSONParser();
		dbm = new HashMap<String, DatabaseFunction>();

		Database db = Implementations.Database();
		dbm.put(ServletConst.CMD_ADD_USER, db::addUser);
		dbm.put(Constants.CMD_ADD_QANS, db::addQuestionnaireAnswers);
		dbm.put(ServletConst.CMD_ADD_CLINIC, db::addClinic);
		dbm.put(Constants.CMD_GET_CLINICS, db::getClinics);
		dbm.put(Constants.CMD_GET_USER, db::getUser);
		dbm.put(Constants.CMD_SET_PASSWORD, db::setPassword);
		dbm.put(Constants.CMD_GET_ERR_MSG, db::getErrorMessages);
		dbm.put(Constants.CMD_GET_INFO_MSG, db::getInfoMessages);
		dbm.put(Constants.CMD_LOAD_Q, db::loadQuestions);
		dbm.put(Constants.CMD_LOAD_QR_DATE, db::loadQResultDates);
		dbm.put(Constants.CMD_LOAD_QR, db::loadQResults);
		dbm.put(Constants.CMD_REQ_REGISTR, db::requestRegistration);
		dbm.put(ServletConst.CMD_RSP_REGISTR, db::respondRegistration);
		dbm.put(Constants.CMD_REQ_LOGIN, db::requestLogin);
		dbm.put(Constants.CMD_REQ_LOGOUT, db::requestLogout);
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
		try
		{
			JSONObject obj = (JSONObject) parser.parse(message);
			return getDBMethod((String) obj.get("command")).dbfunc(obj);
		}
		catch (ParseException pe) { /* unknown format */ }
		catch (NullPointerException _e) { /* unknown command */ }
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
}
