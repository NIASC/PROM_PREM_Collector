/** MySQL_Database.java
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import servlet.core.PPCLogger;
import servlet.core.ServletConst;
import servlet.core.UserManager;
import servlet.core._User;
import servlet.core.interfaces.Database;
import servlet.implementation.exceptions.DBReadException;
import servlet.implementation.exceptions.DBWriteException;
import common.Utilities;
import common.implementation.Constants;

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
public class MySQL_Database implements Database
{
	/* Public */
	
	/**
	 * Retrieves the active instance of this class
	 * 
	 * @return The active instance of this class.
	 */
	public static synchronized MySQL_Database getDatabase()
	{
		if (database == null)
			database = new MySQL_Database();
		return database;
	}

	@Override
	public final Object clone()
			throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	@Override
	public String addUser(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_ADD_USER);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String qInsert = String.format(
				"INSERT INTO `users` (`clinic_id`, `name`, `password`, `email`, `registered`, `salt`, `update_password`) VALUES ('%d', '%s', '%s', '%s', '%s', '%s', '%d')",
				Integer.parseInt(omap.get("clinic_id")),
				omap.get("name"), omap.get("password"), omap.get("email"),
				sdf.format(new Date()), omap.get("salt"), 1);
		try {
			queryUpdate(qInsert);
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		}
		catch (DBWriteException dbw) {
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Database write error", dbw);
		}
		return ret.toString();
	}

	@Override
	public String addQuestionnaireAnswers(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_ADD_QANS);
		
		int clinic_id = Integer.parseInt(omap.get("clinic_id"));
		String identifier = omap.get("identifier");
		String patientInsert = String.format(
				"INSERT INTO `patients` (`clinic_id`, `identifier`, `id`) VALUES ('%d', '%s', NULL)",
				clinic_id, identifier);
		
		Map<String, String> m = (Map<String, String>) getJSONObject(omap.get("questions"));
		List<String> question_ids = new ArrayList<String>();
		List<String> question_answers = new ArrayList<String>();
		for (Entry<String, String> e : m.entrySet())
		{
			question_ids.add((String) e.getKey());
			question_answers.add((String) e.getValue());
		}

		String resultInsert = String.format("INSERT INTO `questionnaire_answers` (`clinic_id`, `patient_identifier`, `date`, %s) VALUES ('%d', '%s', '%s', %s)",
				String.join(", ", question_ids), clinic_id, identifier,
				(new SimpleDateFormat("yyyy-MM-dd")).format(new Date()),
				String.join(", ", question_answers));
		try {
			if (!patientInDatabase(identifier))
				queryUpdate(patientInsert);
			queryUpdate(resultInsert);
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		}
		catch (DBReadException dbr)
		{
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Database read error", dbr);
		}
		catch (SQLException se)
		{
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", se);
		}
		catch (DBWriteException dbw) {
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Database write error", dbw);
		}
		return ret.toString();
	}

	@Override
	public String addClinic(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_ADD_CLINIC);
		
		String qInsert = String.format(
				"INSERT INTO `clinics` (`id`, `name`) VALUES (NULL, '%s')",
				omap.get("name"));
		try
		{
			queryUpdate(qInsert);
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		}
		catch (DBWriteException dbw)
		{
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
			logger.log("Database write error", dbw);
		}
		return ret.toString();
	}
	
	@Override
	public String getClinics(JSONObject obj)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_CLINICS);
		
		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();
			ResultSet rs = query(s, "SELECT `id`, `name` FROM `clinics`");
			
			JSONObject clinics = new JSONObject();
			Map<String, String> cmap = (Map<String, String>) clinics;
			while (rs.next())
				cmap.put(Integer.toString(rs.getInt("id")),
						rs.getString("name"));
			rmap.put("clinics", clinics.toString());
		}
		catch (DBReadException dbr)
		{
			rmap.put("clinics", new JSONObject().toString());
			logger.log("Database read error", dbr);
		}
		catch (SQLException e)
		{
			rmap.put("clinics", new JSONObject().toString());
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return ret.toString();
	}

	@Override
	public _User _getUser(String username)
	{
		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();
			ResultSet rs = query(s, "SELECT `clinic_id`, `name`, `password`, `email`, `salt`, `update_password` FROM `users`");

			_User _user = null;
			while (rs.next()) {
				if (rs.getString("name").equals(username)) {
					_user = new _User();
					_user.clinic_id = rs.getInt("clinic_id");
					_user.name = rs.getString("name");
					_user.password = rs.getString("password");
					_user.email = rs.getString("email");
					_user.salt = rs.getString("salt");
					_user.update_password = rs.getInt("update_password") > 0;
					break;
				}
			}
			return _user;
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException se) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", se);
		}
		return null;
	}

	@Override
	public String setPassword(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;

		JSONObject err = new JSONObject();
		Map<String, String> emap = (Map<String, String>) err;
		emap.put("command", Constants.CMD_GET_ERR_MSG);

		Map<String, String> userobj;
		try
		{
			userobj = getUser(omap.get("name"));
		} catch (Exception e)
		{
			throw new NullPointerException("Can only update password for existing users");
		}
		
		Map<String, String> user = (Map<String, String>) getJSONObject(userobj.get("user"));
		
		String oldPass = omap.get("old_password");
		String newPass = omap.get("new_password");
		String newSalt = omap.get("new_salt");

		if (!user.get("password").equals(oldPass))
		{
			emap.put("user", (new JSONObject()).toString());
			return err.toString();
		}
		
		String qInsert = String.format(
				"UPDATE `users` SET `password`='%s',`salt`='%s',`update_password`=%d WHERE `users`.`name` = '%s'",
				newPass, newSalt, 0, user.get("name"));
		try
		{
			queryUpdate(qInsert);
		}
		catch (DBWriteException dbw)
		{
			logger.log("Database write error", dbw);
			emap.put("user", (new JSONObject()).toString());
			return err.toString();
		}

		JSONObject jobj = new JSONObject();
		try {
			jobj = (JSONObject) getUser(omap.get("name"));
		} catch (Exception e) {
			
		}
		return jobj.toString();
	}

	@Override
	public String getErrorMessages(JSONObject obj)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_ERR_MSG);
		
		getMessages("error_messages", rmap);
		return ret.toString();
	}

	@Override
	public String getInfoMessages(JSONObject obj)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_INFO_MSG);
		
		getMessages("info_messages", rmap);
		return ret.toString();
	}
	
	@Override
	public String loadQuestions(JSONObject obj)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_Q);
		
		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();
			ResultSet rs = query(s, "SELECT * FROM `questionnaire`");
			
			JSONObject questions = new JSONObject();
			Map<String, String> qmap = (Map<String, String>) questions;
			while (rs.next())
			{
				JSONObject question = new JSONObject();
				Map<String, String> questionmap = (Map<String, String>) question;
				for (int i = 0; ; ++i)
				{
					try
					{
						String tbl_name = String.format("option%d", i);
						String entry = rs.getString(tbl_name);
						if (entry == null || (entry = entry.trim()).isEmpty())
							break;
						questionmap.put(tbl_name, entry);
					}
					catch (SQLException e)
					{ /* no more options */
						break;
					}
				}
				String id = Integer.toString(rs.getInt("id"));
				questionmap.put("type", rs.getString("type"));
				questionmap.put("id", id);
				questionmap.put("question", rs.getString("question"));
				questionmap.put("description", rs.getString("description"));
				questionmap.put("optional", Integer.toString(rs.getInt("optional")));
				questionmap.put("max_val", Integer.toString(rs.getInt("max_val")));
				questionmap.put("min_val", Integer.toString(rs.getInt("min_val")));

				qmap.put(id, question.toString());
			}
			rmap.put("questions", questions.toString());
		}
		catch (DBReadException dbr)
		{
			rmap.put("questions", (new JSONObject()).toString());
			logger.log("Database read error", dbr);
		}
		catch (SQLException e)
		{
			rmap.put("questions", (new JSONObject()).toString());
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return ret.toString();
	}

	@Override
	public String loadQResultDates(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_QR_DATE);

		Map<String, String> userobj;
		try
		{
			userobj = getUser(omap.get("name"));
		} catch (Exception e)
		{
			rmap.put("dates", (new JSONArray()).toString());
			logger.log("No user specified");
			return ret.toString();
		}
		
		Map<String, String> user = (Map<String, String>) getJSONObject(userobj.get("user"));

		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();
			ResultSet rs = query(s, String.format(
					"SELECT `date` FROM `questionnaire_answers` WHERE `clinic_id` = %d",
					Integer.parseInt(user.get("clinic_id"))));
			
			JSONArray dates = new JSONArray();
			List<String> dlist = (List<String>) dates;
			while (rs.next())
				dlist.add(rs.getString("date"));
			rmap.put("dates", dates.toString());
		}
		catch (DBReadException dbr) {
			rmap.put("dates", (new JSONArray()).toString());
			logger.log("Database read error", dbr);
		}
		catch (SQLException e) {
			rmap.put("dates", (new JSONArray()).toString());
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return ret.toString();
	}
	
	@Override
	public String loadQResults(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_QR);

		Map<String, String> userobj;
		try
		{
			userobj = getUser(omap.get("name"));
		} catch (Exception e)
		{
			rmap.put("dates", (new JSONObject()).toString());
			logger.log("No user specified");
			return ret.toString();
		}
		
		Map<String, String> user = (Map<String, String>) getJSONObject(userobj.get("user"));
		
		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();

			JSONParser parser = new JSONParser();
			JSONArray questions;
			try{
				questions = (JSONArray) parser.parse(omap.get("questions"));
			} catch (ParseException pe)
			{
				rmap.put("results", (new JSONObject()).toString());
				logger.log("Error parsing JSON object", pe);
				return ret.toString();
			} catch (NullPointerException e)
			{
				rmap.put("results", (new JSONObject()).toString());
				logger.log("Missing 'questions' entry", e);
				return ret.toString();
			}
			
			List<String> qlist = (List<String>) questions;
			
			List<String> lstr = new ArrayList<String>();
			for (Iterator<String> itr = qlist.iterator(); itr.hasNext();)
				lstr.add("`" + itr.next() + "`");
			
			ResultSet rs = query(s, String.format(
					"SELECT %s FROM `questionnaire_answers` WHERE `clinic_id` = %d AND `date` BETWEEN '%s' AND '%s'",
					String.join(", ", lstr), Integer.parseInt(user.get("clinic_id")),
					omap.get("begin"), omap.get("end")));

			JSONArray results = new JSONArray();
			List<String> rlist = (List<String>) results;
			while (rs.next())
			{
				JSONObject answers = new JSONObject();
				Map<String, String> amap = (Map<String, String>) answers;
				for (Iterator<String> itr = qlist.iterator(); itr.hasNext();)
				{
					String q = itr.next();
					amap.put(q, rs.getString(q));
				}
				rlist.add(answers.toString());
			}
			rmap.put("results", results.toString());
		}
		catch (DBReadException dbr)
		{
			rmap.put("results", (new JSONArray()).toString());
			logger.log("Database read error", dbr);
		}
		catch (SQLException e)
		{
			rmap.put("results", (new JSONArray()).toString());
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return ret.toString();
	}
	
	@Override
	public String requestRegistration(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_REQ_REGISTR);
		
		String name = omap.get("name");
		String email = omap.get("email");
		String clinic = omap.get("clinic");
		
		String emailSubject = "PROM_PREM: Registration request";
		String emailDescription = "Registration reguest from";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s:<br><br> %s: %s<br>%s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Name", name, "E-mail",
				email, "Clinic", clinic, emailSignature);
		
		if (send(config.adminEmail, emailSubject, emailBody, "text/html"))
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		else
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		return ret.toString();
	}

	public String respondRegistration(JSONObject obj)
	{
		Map<String, String> omap = (Map<String, String>) obj;
		
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", ServletConst.CMD_RSP_REGISTR);
		
		String username = omap.get("username");
		String email = omap.get("email");
		String password = omap.get("password");
		
		String emailSubject = "PROM_PREM: Registration response";
		String emailDescription = "You have been registered at the PROM/PREM Collector. "
				+ "You will find your login details below. When you first log in you will"
				+ "be asked to update your password.";
		String emailSignature = "This message was sent from the PROM/PREM Collector";
		String emailBody = String.format(
				("%s<br><br> %s: %s<br>%s: %s<br><br> %s"),
				emailDescription, "Username", username,
				"Password", password, emailSignature);
		
		if (send(email, emailSubject, emailBody, "text/html"))
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_SUCCESS);
		else
			rmap.put(Constants.INSERT_RESULT, Constants.INSERT_FAIL);
		return ret.toString();
	}
	
	/* Protected */
	
	/* Private */

	private static MySQL_Database database;
	private static JSONParser parser;
	private static PPCLogger logger = PPCLogger.getLogger();
	
	/**
	 * Handles connection with the database.
	 */
	private DataSource dataSource;
	
	/**
	 * Configuration data for sending an email from the servlet's email
	 * to the admin's email.
	 */
	private EmailConfig config;
	
	static
	{
		parser = new JSONParser();
	}
	
	/**
	 * Initializes variables and loads the database configuration.
	 * This class is a singleton and should only be instantiated once.
	 */
	private MySQL_Database()
	{
		try
		{
			config = new EmailConfig();
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/prom_prem_db");
		}
		catch (NamingException e)
		{
			logger.log("FATAL: Could not load database configuration", e);
			System.exit(1);
		}
		catch (IOException e)
		{
			logger.log("FATAL: Could not load email configuration", e);
			System.exit(1);
		}
	}
	
	/**
	 * Checks if a patient with the {@code identifier} exists in the database.
	 * 
	 * @param identifier The identifier of the patient.
	 * 
	 * @return {@code true} if the patient exists in the database,
	 * 		{@code false} if not.
	 */
	private boolean patientInDatabase(String identifier)
			throws SQLException, DBReadException
	{
		Connection conn = dataSource.getConnection();
		Statement s = conn.createStatement();
		ResultSet rs = query(s, "SELECT `identifier` FROM `patients`");
		
		while (rs.next())
			if (rs.getString("identifier").equals(identifier))
				return true;
		return false;
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
	private Map<String, String> getUser(String username) throws Exception
	{
		_User _user = _getUser(username);

		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_USER);
		
		JSONObject user = new JSONObject();
		Map<String, String> umap = (Map<String, String>) user;
		umap.put("clinic_id", Integer.toString(_user.clinic_id));
		umap.put("name", _user.name);
		umap.put("password", _user.password);
		umap.put("email", _user.email);
		umap.put("salt", _user.salt);
		umap.put("update_password", _user.update_password ? "1" : "0");
		
		rmap.put("user", user.toString());
		return (Map<String, String>) ret;
	}
	
	/**
	 * Query the database to update an entry i.e. modify an existing
	 * database entry.
	 * 
	 * @param message The command (specified by the SQL language)
	 * 		to send to the database.
	 * 
	 * @return QUERY_SUCCESS on successful query, ERROR on failure.
	 * 
	 * @throws DBWriteException If an update error occurs.
	 */
	private void queryUpdate(String message) throws DBWriteException
	{
		try (Connection c = dataSource.getConnection())
		{
			c.createStatement().executeUpdate(message);
		}
		catch (SQLException se)
		{
			throw new DBWriteException(String.format(
					"Database could not process request: '%s'. Check your arguments.",
					message));
		}
	}
	
	/**
	 * Query the database, typically for data (i.e. request data from
	 * the database).
	 * 
	 * @param s The statement that executes the query. The statement
	 * 		can be acquired by calling
	 * 		Connection c = DriverManager.getConnection(...)
	 * 		Statement s = c.createStatement()
	 * @param message The command (specified by the SQL language)
	 * 		to send to the database.
	 * 
	 * @return The the ResultSet from the database.
	 * 
	 * @throws DBReadException If the statement is not initialized or a
	 * 		query error occurs.
	 */
	private ResultSet query(Statement s, String message) throws DBReadException
	{
		try
		{
			return s.executeQuery(message);
		}
		catch (SQLException se)
		{
			throw new DBReadException(String.format(
					"Database could not process request: '%s'. Check your arguments.",
					message));
		}
	}

	/**
	 * Retrieves messages from the database and places them in
	 * {@code retobj}
	 * 
	 * @param tableName The name of the (message) table to retrieve
	 * 		messages from.
	 * 
	 * @param retobj The map to put the messages in.
	 * 
	 * @return true if the messages were put in the map.
	 */
	private boolean getMessages(String tableName, Map<String, String> retobj)
	{
		boolean ret = false;
		try (Connection conn = dataSource.getConnection())
		{
			Statement s = conn.createStatement();
			ResultSet rs = query(s, String.format(
					"SELECT `code`, `name`, `locale`, `message` FROM `%s`",
					tableName));
			
			JSONObject messages = new JSONObject();
			Map<String, String> mmap = (Map<String, String>) messages;
			while (rs.next())
			{
				JSONObject msg = new JSONObject();
				Map<String, String> msgmap = (Map<String, String>) msg;
				msgmap.put(rs.getString("locale"), rs.getString("message"));

				JSONObject message = new JSONObject();
				Map<String, String> messagemap = (Map<String, String>) message;
				String name = rs.getString("name");
				messagemap.put("name", name);
				messagemap.put("code", rs.getString("code"));
				messagemap.put("message", msg.toString());

				mmap.put(name, message.toString());
			}
			retobj.put("messages", messages.toString());
			ret = true;
		}
		catch (DBReadException dbr) {
			retobj.put("messages", (new JSONObject()).toString());
			logger.log("Database read error", dbr);
		}
		catch (SQLException e) {
			retobj.put("messages", (new JSONObject()).toString());
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return ret;
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
		catch (ParseException pe)
		{
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
	private JSONArray getJSONArray(String str)
	{
		try
		{
			return (JSONArray) parser.parse(str);
		}
		catch (ParseException pe)
		{
			throw new NullPointerException("JSON parse error");
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
	private boolean send(String recipient, String emailSubject,
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