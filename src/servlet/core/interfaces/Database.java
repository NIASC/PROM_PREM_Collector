/** Database.java
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
package servlet.core.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

import servlet.core.User;
import servlet.core._Message;
import servlet.core._Question;


/**
 * This interface contains the methods required by the core part of
 * this program to function. The purpose of this interface is to give
 * the freedom of choosing your own database along with the core part
 * of this program.
 * 
 * @author Marcus Malmquist
 *
 */
public interface Database
{
	/* Public */
	
	/**
	 * Replaces database escape characters with a safe equivalent.
	 * 
	 * @param str The string to process
	 * @return The database-safe equivalent of the input string.
	 */
	String escapeReplace(String str);

	String escapeReplace(List<String> str);
	
	boolean isSQLList(String s);
	
	List<String> SQLListToJavaList(String sqlList)
			throws IllegalArgumentException;
	
	/**
	 * Adds a user contained in {@code obj} to the database.
	 * 
	 * @param obj The JSONObject that contains the user data.
	 * 
	 * @return A JSONObject with information about if the user
	 * 		was added.
	 */
	public boolean addUser(int clinic_id, String name,
			String password, String email, String salt);
	
	boolean addPatient(int clinic_id, String identifier);
	
	/**
	 * Adds questionnaire answers contained in {@code obj} to the
	 * database.
	 * 
	 * @param obj The JSONObject that contains the questionnaire answer
	 * 		data.
	 * 
	 * @return A JSONObject with information about if the answers
	 * 		were added.
	 */
	public boolean addQuestionnaireAnswers(
			int clinic_id, String identifier,
			List<String> question_answers);
	
	/**
	 * Adds a clinic contained in {@code obj} to the database.
	 * 
	 * @param obj The JSONObject that contains the clinic data.
	 * 
	 * @return A JSONObject with information about if the clinic
	 * 		was added.
	 */
	public boolean addClinic(String name);
	
	/**
	 * Retrieves the clinics from the database.
	 * 
	 * @param obj The JSONObject that contains the request.
	 * 
	 * @return A JSONObject that contains the clinics.
	 */
	public Map<Integer, String> getClinics();
	
	/**
	 * Retrieves the user from the database.
	 * 
	 * @param obj The JSONObject that contains the request, including
	 * 		which user to retrieve.
	 * 
	 * @return A JSONObject that contains the user.
	 */
	public User getUser(String username);
	
	/**
	 * Updates the password for the user contained in {@code obj}.
	 * 
	 * @param obj The JSONObject that contains the new password,
	 * 		new salt, old password and the username.
	 * 
	 * @return A JSONObject that contains the user with the new password.
	 */
	public boolean setPassword(String name, String oldPass,
			String newPass, String newSalt);
	
	/**
	 * Retrieves the questionnaire questions from the database.
	 * 
	 * @param obj The JSONObject that contains the request.
	 * 
	 * @return A JSONObject that contains the questionnaire questions.
	 */
	public Map<Integer, _Question> loadQuestions();
	
	/**
	 * Retrieves the dates that questionnaire answers were added to the
	 * database.
	 * 
	 * @param obj The JSONObject that contains the request.
	 * 
	 * @return A JSONObject that contains the dates.
	 */
	public List<String> loadQResultDates(int clinic_id);
	
	/**
	 * Retrieves the questionnaire results from the database.
	 * 
	 * @param obj The JSONObject that contains the request, including
	 * 		the upper and lower limit as well as which questions to
	 * 		retrieve.
	 * 
	 * @return A JSONObject that contains the questionnaire results.
	 */
	public List<Map<Integer, String>> loadQResults(
			int clinic_id, List<Integer> qlist, Date begin, Date end);
	
	/* Protected */
	
	/* Private */
}
