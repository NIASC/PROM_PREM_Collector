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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import servlet.core.PPCLogger;
import servlet.core.User;
import servlet.core._Question;
import servlet.core.interfaces.Database;
import servlet.implementation.exceptions.DBReadException;
import servlet.implementation.exceptions.DBWriteException;

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
	public String escapeReplace(String str)
	{
		return String.format("'%s'", str.replace("\'", "\""));
	}

	@Override
	public String escapeReplace(List<String> lstr)
	{
		List<String> out = new ArrayList<>();
		for (String str : lstr)
			out.add(escapeReplace(str));
		return String.format("[%s]", String.join(",", out));
	}
	
	public boolean isSQLList(String s)
	{
		return s.startsWith("[") && s.endsWith("]");
	}
	
	public List<String> SQLListToJavaList(String l)
			throws IllegalArgumentException
	{
		if (!isSQLList(l))
			throw new IllegalArgumentException("Not an SQL list");
		return Arrays.asList(l.substring(1, l.length()-1).split(","));
	}

	@Override
	public boolean addUser(int clinic_id, String name,
			String password, String email, String salt)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String qInsert = String.format(
				"INSERT INTO `users` (`clinic_id`, `name`, `password`, `email`, `registered`, `salt`, `update_password`) VALUES ('%d', '%s', '%s', '%s', '%s', '%s', '%d')",
				clinic_id,
				_escapeReplace(name),
				_escapeReplace(password),
				_escapeReplace(email),
				sdf.format(new Date()),
				_escapeReplace(salt), 1);
		try {
			queryUpdate(qInsert);
			return true;
		}
		catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
			return false;
		}
	}
	
	@Override
	public boolean addPatient(int clinic_id, String identifier)
	{
		String patientInsert = String.format(
				"INSERT INTO `patients` (`clinic_id`, `identifier`, `id`) VALUES ('%d', '%s', NULL)",
				clinic_id,
				_escapeReplace(identifier));
		try {
			if (!patientInDatabase(identifier))
				queryUpdate(patientInsert);
			return true;
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException se) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", se);
		} catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
		}
		return false;
	}

	@Override
	public boolean addQuestionnaireAnswers(
			int clinic_id, String identifier,
			List<String> question_answers)
	{
		List<String> question_ids = new ArrayList<String>();
		
		for (int i = 0; i < question_answers.size(); ++i)
			question_ids.add(String.format("`question%d`", i));

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String resultInsert = String.format("INSERT INTO `questionnaire_answers` (`clinic_id`, `patient_identifier`, `date`, %s) VALUES ('%d', '%s', '%s', %s)",
				String.join(", ", question_ids),
				clinic_id,
				_escapeReplace(identifier),
				sdf.format(new Date()),
				String.join(", ", question_answers));
		try {
			queryUpdate(resultInsert);
			return true;
		} catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
			return false;
		}
	}

	@Override
	public boolean addClinic(String name)
	{
		String qInsert = String.format(
				"INSERT INTO `clinics` (`id`, `name`) VALUES (NULL, '%s')",
				_escapeReplace(name));
		try {
			queryUpdate(qInsert);
			return true;
		} catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
			return false;
		}
	}
	
	@Override
	public Map<Integer, String> getClinics()
	{
		Map<Integer, String> cmap = new TreeMap<Integer, String>();
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = query(conn.createStatement(),
					"SELECT `id`, `name` FROM `clinics`");
			while (rs.next())
				cmap.put(rs.getInt("id"), rs.getString("name"));
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return cmap;
	}

	@Override
	public User getUser(String username)
	{
		String q = String.format("SELECT `clinic_id`, `name`, `password`, `email`, `salt`, `update_password` FROM `users` WHERE `users`.`name`='%s'",
				_escapeReplace(username));
		try (Connection conn = dataSource.getConnection())
		{
			ResultSet rs = query(conn.createStatement(), q);

			User _user = null;
			if (rs.next()) {
				_user = new User();
				_user.clinic_id = rs.getInt("clinic_id");
				_user.name = rs.getString("name");
				_user.password = rs.getString("password");
				_user.email = rs.getString("email");
				_user.salt = rs.getString("salt");
				_user.update_password = rs.getInt("update_password") > 0;
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
	public boolean setPassword(String name, String oldPass,
			String newPass, String newSalt)
	{
		User _user = getUser(name);
		if (!_user.password.equals(_escapeReplace(oldPass))) {
			return false;
		}
		
		String qInsert = String.format(
				"UPDATE `users` SET `password`='%s',`salt`='%s',`update_password`='%d' WHERE `users`.`name`='%s'",
				_escapeReplace(newPass),
				_escapeReplace(newSalt),
				0,
				_escapeReplace(name));
		try {
			queryUpdate(qInsert);
			return true;
		} catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
			return false;
		}
	}
	
	@Override
	public Map<Integer, _Question> loadQuestions()
	{
		Map<Integer, _Question> _questions = new HashMap<Integer, _Question>();
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = query(conn.createStatement(),
					"SELECT * FROM `questionnaire`");
			
			while (rs.next()) {
				_Question _question = new _Question();
				for (int i = 0; ; ++i) {
					try {
						String entry = rs.getString(String.format("option%d", i));
						if (entry == null || (entry = entry.trim()).isEmpty())
							break;
						_question.options.add(i, entry);
					} catch (SQLException e) {
						/* no more options */
						break;
					}
				}
				int id = rs.getInt("id");
				_question.type = rs.getString("type");
				_question.id = id;
				_question.question = rs.getString("question");
				_question.description = rs.getString("description");
				_question.optional = rs.getInt("optional") > 0;
				_question.max_val = rs.getInt("max_val");
				_question.min_val = rs.getInt("min_val");

				_questions.put(id, _question);
			}
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return _questions;
	}

	@Override
	public List<String> loadQResultDates(int clinic_id)
	{
		String q = String.format(
				"SELECT `date` FROM `questionnaire_answers` WHERE `clinic_id`='%d'",
				clinic_id);
		List<String> dlist = new ArrayList<String>();
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = query(conn.createStatement(), q);
			
			while (rs.next())
				dlist.add(rs.getString("date"));
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return dlist;
	}
	
	@Override
	public List<Map<String, String>> loadQResults(int clinic_id,
			List<Integer> qlist,
			Date begin, Date end)
	{
		List<Map<String, String>> _results = new ArrayList<Map<String, String>>();
		try (Connection conn = dataSource.getConnection())
		{
			List<String> lstr = new ArrayList<String>();
			for (Integer i : qlist)
				lstr.add(String.format("`question%d`", i));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String q = String.format(
					"SELECT %s FROM `questionnaire_answers` WHERE `clinic_id`='%d' AND `date` BETWEEN '%s' AND '%s'",
					String.join(", ", lstr),
					clinic_id,
					sdf.format(begin),
					sdf.format(end));
			ResultSet rs = query(conn.createStatement(), q);

			while (rs.next()) {
				Map<String, String> _answers = new HashMap<String, String>();
				for (Integer i : qlist) {
					String str = String.format("question%d", i);
					_answers.put(str, rs.getString(str));
				}
				_results.add(_answers);
			}
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database "
					+ "or while parsing SQL ResultSet", e);
		}
		return _results;
	}
	
	/* Protected */
	
	/* Private */

	private static MySQL_Database database;
	private static PPCLogger logger = PPCLogger.getLogger();
	
	/**
	 * Handles connection with the database.
	 */
	private DataSource dataSource;
	
	/**
	 * Initializes variables and loads the database configuration.
	 * This class is a singleton and should only be instantiated once.
	 */
	private MySQL_Database()
	{
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/prom_prem_db");
		} catch (NamingException e) {
			logger.log("FATAL: Could not load database configuration", e);
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
		String q = String.format(
				"SELECT `identifier` FROM `patients` where `patients`.`identifier`='%s'",
				_escapeReplace(identifier));
		ResultSet rs = query(conn.createStatement(), q);
		boolean exsist = rs.next();
		conn.close();
		
		return exsist;
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
		try (Connection c = dataSource.getConnection()) {
			c.createStatement().executeUpdate(message);
		} catch (SQLException se) {
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
		try {
			return s.executeQuery(message);
		} catch (SQLException se) {
			throw new DBReadException(String.format(
					"Database could not process request: '%s'. Check your arguments.",
					message));
		}
	}
	
	private String _escapeReplace(String str)
	{
		return String.format("%s", str.replace("\'", "\""));
	}
}