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

import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.implementation.exceptions.DBReadException;
import servlet.implementation.exceptions.DBWriteException;

public enum MySQLDatabase implements Database
{
	instance;

	@Override
	public String escapeReplace(String str) {
		return String.format("'%s'", str.replace("\'", "\""));
	}

	@Override
	public String escapeReplace(List<String> lstr) {
		List<String> out = new ArrayList<>();
		for (String str : lstr) {
			out.add(escapeReplace(str));
		}
		return String.format("[%s]", String.join(",", out));
	}
	
	public boolean isSQLList(String s) {
		return s.startsWith("[") && s.endsWith("]");
	}
	
	public List<String> SQLListToJavaList(String l) throws IllegalArgumentException {
		if (!isSQLList(l)) {
			throw new IllegalArgumentException("Not an SQL list");
		}
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
			writeToDatabase(qInsert);
			return true;
		} catch (DBWriteException dbw) {
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
				writeToDatabase(patientInsert);
			return true;
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException se) {
			logger.log("Error opening connection to database or while parsing SQL ResultSet", se);
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
			writeToDatabase(resultInsert);
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
			writeToDatabase(qInsert);
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
			ResultSet rs = readFromDatabase(conn, "SELECT `id`, `name` FROM `clinics`");
			while (rs.next()) {
				cmap.put(rs.getInt("id"), rs.getString("name"));
			}
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
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = readFromDatabase(conn, q);

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
			writeToDatabase(qInsert);
			return true;
		} catch (DBWriteException dbw) {
			logger.log("Database write error", dbw);
			return false;
		}
	}
	
	@Override
	public Map<Integer, QuestionData> loadQuestions()
	{
		Map<Integer, QuestionData> _questions = new HashMap<Integer, QuestionData>();
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = readFromDatabase(conn,
					"SELECT * FROM `questionnaire`");
			
			while (rs.next()) {
				QuestionData _question = new QuestionData();
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
			logger.log("Error opening connection to database or while parsing SQL ResultSet", e);
		}
		return _questions;
	}

	@Override
	public List<String> loadQuestionResultDates(int clinic_id)
	{
		String q = String.format(
				"SELECT `date` FROM `questionnaire_answers` WHERE `clinic_id`='%d'",
				clinic_id);
		List<String> dlist = new ArrayList<String>();
		try (Connection conn = dataSource.getConnection()) {
			ResultSet rs = readFromDatabase(conn, q);
			
			while (rs.next()) {
				dlist.add(rs.getString("date"));
			}
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database or while parsing SQL ResultSet", e);
		}
		return dlist;
	}
	
	@Override
	public List<Map<Integer, String>> loadQuestionResults(int clinic_id,
			List<Integer> qlist,
			Date begin, Date end)
	{
		List<Map<Integer, String>> _results = new ArrayList<Map<Integer, String>>();
		try (Connection conn = dataSource.getConnection()) {
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
			ResultSet rs = readFromDatabase(conn, q);

			while (rs.next()) {
				Map<Integer, String> _answers = new HashMap<Integer, String>();
				for (Integer i : qlist) {
					String str = String.format("question%d", i);
					_answers.put(i, rs.getString(str));
				}
				_results.add(_answers);
			}
		} catch (DBReadException dbr) {
			logger.log("Database read error", dbr);
		} catch (SQLException e) {
			logger.log("Error opening connection to database or while parsing SQL ResultSet", e);
		}
		return _results;
	}

	private static ServletLogger logger;
	
	static {
		logger = ServletLogger.LOGGER;
	}
	
	private DataSource dataSource;
	
	private MySQLDatabase()
	{
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/prom_prem_db");
		} catch (NamingException e) {
			ServletLogger.LOGGER.log("FATAL: Could not load database configuration", e);
			System.exit(1);
		}
	}
	
	private boolean patientInDatabase(String identifier) throws SQLException, DBReadException
	{
		Connection conn = dataSource.getConnection();
		String q = String.format(
				"SELECT `identifier` FROM `patients` where `patients`.`identifier`='%s'",
				_escapeReplace(identifier));
		ResultSet rs = readFromDatabase(conn, q);
		boolean exsist = rs.next();
		conn.close();
		
		return exsist;
	}
	
	private void writeToDatabase(String query) throws DBWriteException
	{
		try (Connection c = dataSource.getConnection()) {
			c.createStatement().executeUpdate(query);
		} catch (SQLException se) {
			throw new DBWriteException(String.format(
					"Database could not process request: '%s'. Check your arguments.",
					query));
		}
	}
	
	private ResultSet readFromDatabase(Connection conn, String query) throws DBReadException
	{
		try {
			return conn.createStatement().executeQuery(query);
		} catch (SQLException se) {
			throw new DBReadException(String.format(
					"Database could not process request: '%s'. Check your arguments.",
					query));
		}
	}
	
	private String _escapeReplace(String str) {
		return String.format("%s", str.replace("\'", "\""));
	}
}