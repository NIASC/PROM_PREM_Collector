package niasc.servlet.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.PhonyLogger;
import servlet.core._Logger;
import servlet.implementation.MySQLDatabase;
import servlet.implementation.QuestionData;
import servlet.implementation.User;

public class MySQLDatabaseTest {
	MySQLDatabase db;
	_Logger logger;
	
	PhonyDataSource ds;
	PhonyConnection c;
	PhonyStatement s;
	PhonyResultSet rs;

	@Before
	public void setUp() throws Exception {
		logger = new PhonyLogger();
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		c = new PhonyConnection(s);
		ds = new PhonyDataSource(c);
		db = new MySQLDatabase(ds, logger);
	}

	@Test
	public void testEscapeReplaceAndConvertToSQLEntry() {
		Assert.assertEquals("'test'", db.escapeReplaceAndConvertToSQLEntry("test"));
		Assert.assertEquals("'\"test\"'", db.escapeReplaceAndConvertToSQLEntry("'test'"));
		Assert.assertEquals("'\"test\"'", db.escapeReplaceAndConvertToSQLEntry("\"test\""));
		Assert.assertEquals("'drop database phony_db;'", db.escapeReplaceAndConvertToSQLEntry("drop database phony_db;"));
		Assert.assertEquals("'\"; drop database phony_db; \"'", db.escapeReplaceAndConvertToSQLEntry("'; drop database phony_db; '"));
		Assert.assertEquals(null, db.escapeReplaceAndConvertToSQLEntry((String)null));
	}

	@Test
	public void testEscapeReplaceAndConvertToSQLListOfEntries() {
		Assert.assertEquals("[\"test0\",\"test1\"]", db.escapeReplaceAndConvertToSQLListOfEntries(Arrays.asList("test0", "test1")));
		Assert.assertEquals("[\"\"test0\"\",\"test1\"]", db.escapeReplaceAndConvertToSQLListOfEntries(Arrays.asList("'test0'", "test1")));
		Assert.assertEquals("[\"test0\",\"\"; drop database phony_db; \"\",\"test2\"]", db.escapeReplaceAndConvertToSQLListOfEntries(Arrays.asList("test0", "'; drop database phony_db; '", "test2")));
		Assert.assertEquals("[]", db.escapeReplaceAndConvertToSQLListOfEntries(new ArrayList<String>()));
		Assert.assertEquals(null, db.escapeReplaceAndConvertToSQLListOfEntries((List<String>)null));
	}

	@Test
	public void testIsSQLList() {
		Assert.assertTrue(db.isSQLList("['test0']"));
		Assert.assertTrue(db.isSQLList("['test0','test1']"));
		Assert.assertTrue(db.isSQLList("['\"test0\"','test1']"));
		Assert.assertTrue(db.isSQLList("[test0,test1]"));
		Assert.assertFalse(db.isSQLList("{test0,test1}"));
		Assert.assertFalse(db.isSQLList("{test0}"));
		Assert.assertFalse(db.isSQLList("test0"));
	}

	@Test
	public void testSQLListToJavaList() {
		Assert.assertEquals(Arrays.asList("'test0'", "'test1'"), db.SQLListToJavaList("['test0','test1']"));
		Assert.assertEquals(Arrays.asList("test0", "test1"), db.SQLListToJavaList("[test0,test1]"));
		try {
			List<String> l = db.SQLListToJavaList("{test0,test1}");
			Assert.fail("Invalid SQLList successfully converted to Java List");
		} catch (IllegalArgumentException e) { }
	}

	@Test
	public void testAddUser() {
		Assert.assertTrue(db.addUser(0, "dummy", "s3cre3t", "dummy@phony.com", "s4lt"));
		Assert.assertEquals(String.format("INSERT INTO `users` (`clinic_id`, `name`, `password`, `email`, `registered`, `salt`, `update_password`) VALUES ('0', 'dummy', 's3cre3t', 'dummy@phony.com', '%s', 's4lt', '1')", new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
				s.getLastSQLUpdate());
		Assert.assertTrue(db.addUser(0, "'; drop database phony_db; '", "s3cre3t", "dummy@phony.com", "s4lt"));
		Assert.assertEquals(String.format("INSERT INTO `users` (`clinic_id`, `name`, `password`, `email`, `registered`, `salt`, `update_password`) VALUES ('0', '\"; drop database phony_db; \"', 's3cre3t', 'dummy@phony.com', '%s', 's4lt', '1')", new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
				s.getLastSQLUpdate());
	}

	@Test
	public void testAddPatient() {
		Assert.assertTrue(db.addPatient(0, "patientIdentifier"));
		Assert.assertEquals("INSERT INTO `patients` (`clinic_id`, `identifier`, `id`) VALUES ('0', 'patientIdentifier', NULL)",
				s.getLastSQLUpdate());
		Assert.assertTrue(db.addPatient(0, "'; drop database phony_db; '"));
		Assert.assertEquals("INSERT INTO `patients` (`clinic_id`, `identifier`, `id`) VALUES ('0', '\"; drop database phony_db; \"', NULL)",
				s.getLastSQLUpdate());
	}

	@Test
	public void testAddQuestionnaireAnswers() {
		Assert.assertTrue(db.addQuestionnaireAnswers(0, "patientIdentifier", Arrays.asList("2", "option0", "'; drop database phony_db; '", "['option0','option1']")));
		Assert.assertEquals(String.format("INSERT INTO `questionnaire_answers` (`clinic_id`, `patient_identifier`, `date`, `question0`, `question1`, `question2`, `question3`) VALUES ('0', 'patientIdentifier', '%s', '2', 'option0', '\"; drop database phony_db; \"', '[\"option0\",\"option1\"]')", new SimpleDateFormat("yyyy-MM-dd").format(new Date())),
				s.getLastSQLUpdate());
	}

	@Test
	public void testAddClinic() {
		Assert.assertTrue(db.addClinic("dummy"));
		Assert.assertEquals("INSERT INTO `clinics` (`id`, `name`) VALUES (NULL, 'dummy')", s.getLastSQLUpdate());
		Assert.assertTrue(db.addClinic("'; drop database phony_db; '"));
		Assert.assertEquals("INSERT INTO `clinics` (`id`, `name`) VALUES (NULL, '\"; drop database phony_db; \"')", s.getLastSQLUpdate());
	}

	@Test
	public void testGetClinics() {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("id", 0);
		rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		rs.setNextStrings(strings);
		rs.setNumberOfAvailableNextCalls(1);
		Map<Integer, String> clinics = db.getClinics();
		Assert.assertEquals("SELECT `id`, `name` FROM `clinics`", s.getLastSQLQuery());
		Assert.assertFalse(clinics.isEmpty());
		Assert.assertTrue(clinics.containsKey(0));
		Assert.assertEquals("phony", clinics.get(0));
	}

	@Test
	public void testGetUser() {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		rs.setNextStrings(strings);
		User user = db.getUser("phony");
		Assert.assertEquals("SELECT `clinic_id`, `name`, `password`, `email`, `salt`, `update_password` FROM `users` WHERE `users`.`name`='phony'", s.getLastSQLQuery());
		Assert.assertNull(user);
		rs.setNumberOfAvailableNextCalls(1);
		
		user = db.getUser("phony");
		Assert.assertEquals("SELECT `clinic_id`, `name`, `password`, `email`, `salt`, `update_password` FROM `users` WHERE `users`.`name`='phony'", s.getLastSQLQuery());
		Assert.assertNotNull(user);
		Assert.assertEquals(0, user.clinic_id);
		Assert.assertEquals(true, user.update_password);
		Assert.assertEquals("phony", user.name);
		Assert.assertEquals("s3cr3t", user.password);
		Assert.assertEquals("s4lt", user.salt);
	}

	@Test
	public void testSetPassword() {
		/* set up user data */
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		rs.setNextStrings(strings);
		rs.setNumberOfAvailableNextCalls(1);
		
		Assert.assertTrue(db.setPassword("phony", "s3cr3t", "p4ssw0rd", "s4lt"));
		Assert.assertEquals("UPDATE `users` SET `password`='p4ssw0rd',`salt`='s4lt',`update_password`='0' WHERE `users`.`name`='phony'",
				s.getLastSQLUpdate());

		/* set up user data */
		rs.setNextInts(ints);
		rs.setNextStrings(strings);
		rs.setNumberOfAvailableNextCalls(1);
		Assert.assertTrue(db.setPassword("phony", "s3cr3t", "'; drop database phony_db; '", "s4lt"));
		Assert.assertEquals("UPDATE `users` SET `password`='\"; drop database phony_db; \"',`salt`='s4lt',`update_password`='0' WHERE `users`.`name`='phony'",
				s.getLastSQLUpdate());
	}

	@Test
	public void testLoadQuestions() {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("id", 999);
		ints.put("optional", 0);
		ints.put("max_val", 5);
		ints.put("min_val", 1);
		rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("option0", "Hello");
		strings.put("option1", "Hi");
		strings.put("option2", "Greetings");
		strings.put("type", "SingleOption");
		strings.put("question", "Say hi");
		strings.put("description", "Choose how to say hi");
		rs.setNextStrings(strings);
		rs.setNumberOfAvailableNextCalls(1);
				
		Map<Integer, QuestionData> q = db.loadQuestions();
		Assert.assertEquals("SELECT * FROM `questionnaire`", s.getLastSQLQuery());
		Assert.assertFalse(q.isEmpty());
		Assert.assertTrue(q.containsKey(999));
		QuestionData qd = q.get(999);
		Assert.assertEquals(999, qd.id);
		Assert.assertFalse(qd.optional);
		Assert.assertEquals(5, qd.max_val);
		Assert.assertEquals(1, qd.min_val);
		Assert.assertEquals(Arrays.asList("Hello", "Hi", "Greetings"), qd.options);
		Assert.assertEquals("SingleOption", qd.type);
		Assert.assertEquals("Say hi", qd.question);
		Assert.assertEquals("Choose how to say hi", qd.description);
	}

	@Test
	public void testLoadQuestionResultDates() {
		Map<String, String> name = new HashMap<String, String>();
		name.put("date", "1970-01-01");
		rs.setNextStrings(name);
		rs.setNumberOfAvailableNextCalls(1);
		
		List<String> dates = db.loadQuestionResultDates(0);
		Assert.assertEquals("SELECT `date` FROM `questionnaire_answers` WHERE `clinic_id`='0'", s.getLastSQLQuery());
		Assert.assertFalse(dates.isEmpty());
		Assert.assertEquals(Arrays.asList("1970-01-01"), dates);
	}

	@Test
	public void testLoadQuestionResults() throws Exception {
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("question0", "2");
		strings.put("question1", "option0");
		strings.put("question2", "\"; drop database phony_db; \"");
		strings.put("question3", "[\"option0\",\"option1\"]");
		rs.setNextStrings(strings);
		rs.setNumberOfAvailableNextCalls(1);
		
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
		List<Map<Integer, String>> result = db.loadQuestionResults(999, Arrays.asList(2, 3), parser.parse("1970-01-01"), parser.parse("1999-12-31"));
		Assert.assertEquals("SELECT `question2`, `question3` FROM `questionnaire_answers` WHERE `clinic_id`='999' AND `date` BETWEEN '1970-01-01' AND '1999-12-31'",
				s.getLastSQLQuery());
		Assert.assertFalse(result.isEmpty());
		Map<Integer, String> answers = result.get(0);
		Assert.assertNotNull(answers);
		Assert.assertEquals(2, answers.size());
		Assert.assertNull(answers.get(1));
		Assert.assertEquals("\"; drop database phony_db; \"", answers.get(2));
		Assert.assertEquals("[\"option0\",\"option1\"]", answers.get(3));
		Assert.assertNull(answers.get(4));
	}

}
