package niasc.phony;

import java.util.Date;
import java.util.List;
import java.util.Map;

import servlet.core.PPCDatabase;
import servlet.implementation.QuestionData;
import servlet.implementation.User;

public class PhonyDatabase implements PPCDatabase {
	@Override
	public String escapeReplaceAndConvertToSQLEntry(String str) { return null; }
	@Override
	public String escapeReplaceAndConvertToSQLListOfEntries(List<String> str) { return null; }
	@Override
	public boolean isSQLList(String s) { return false; }
	@Override
	public List<String> SQLListToJavaList(String sqlList) throws IllegalArgumentException { return null; }
	@Override
	public boolean addUser(int clinic_id, String name, String password, String email, String salt) { return false; }
	@Override
	public boolean addPatient(int clinic_id, String identifier) { return false; }
	@Override
	public boolean addQuestionnaireAnswers(int clinic_id, String identifier, List<String> question_answers) { return false; }
	@Override
	public boolean addClinic(String name) { return false; }
	@Override
	public Map<Integer, String> getClinics() { return null; }
	@Override
	public User getUser(String username) { return null; }
	@Override
	public boolean setPassword(String name, String oldPass, String newPass, String newSalt) { return false; }
	@Override
	public Map<Integer, QuestionData> loadQuestions() { return null; }
	@Override
	public List<String> loadQuestionResultDates(int clinic_id) { return null; }
	@Override
	public List<Map<Integer, String>> loadQuestionResults(int clinic_id, List<Integer> qlist, Date begin, Date end) { return null; }

}
