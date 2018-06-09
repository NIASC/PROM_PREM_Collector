package se.nordicehealth.servlet.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

import se.nordicehealth.servlet.implementation.QuestionData;
import se.nordicehealth.servlet.implementation.User;

public interface PPCDatabase
{
	String escapeReplaceAndConvertToSQLEntry(String str);
	String escapeReplaceAndConvertToSQLListOfEntries(List<String> str);
	boolean isSQLList(String s);
	List<String> SQLListToJavaList(String sqlList) throws IllegalArgumentException;
	
	public boolean addUser(int clinic_id, String name, String password, String email, String salt);
	boolean addPatient(int clinic_id, String identifier);
	public boolean addQuestionnaireAnswers(int clinic_id, String identifier, List<String> question_answers);
	public boolean addClinic(String name);
	public Map<Integer, String> getClinics();
	public User getUser(String username);
	public boolean setPassword(String name, String oldPass, String newPass, String newSalt);
	public Map<Integer, QuestionData> loadQuestions();
	public List<String> loadQuestionResultDates(int clinic_id);
	public List<Map<Integer, String>> loadQuestionResults(int clinic_id, List<Integer> qlist, Date begin, Date end);
}
