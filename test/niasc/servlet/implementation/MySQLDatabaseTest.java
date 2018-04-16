package niasc.servlet.implementation;

import java.text.SimpleDateFormat;
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
	public void testEscapeReplaceString() {
		Assert.assertEquals("'test'", db.escapeReplace("test"));
		Assert.assertEquals("'\"test\"'", db.escapeReplace("'test'"));
		Assert.assertEquals("'\"test\"'", db.escapeReplace("\"test\""));
		Assert.assertEquals("'drop database phony_db;'", db.escapeReplace("drop database phony_db;"));
		Assert.assertEquals("'\"; drop database phony_db; \"'", db.escapeReplace("'; drop database phony_db; '"));
	}

	@Test
	public void testEscapeReplaceListOfString() {
		Assert.assertEquals("['test0','test1']", db.escapeReplace(Arrays.asList("test0", "test1")));
		Assert.assertEquals("['\"test0\"','test1']", db.escapeReplace(Arrays.asList("'test0'", "test1")));
		Assert.assertEquals("['test0','\"; drop database phony_db; \"','test2']", db.escapeReplace(Arrays.asList("test0", "'; drop database phony_db; '", "test2")));
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
		Map<String, Integer> id = new HashMap<String, Integer>();
		id.put("id", 0);
		rs.setNextInts(id);
		Map<String, String> name = new HashMap<String, String>();
		name.put("name", "phony");
		rs.setNextStrings(name);
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
		Map<String, String> name = new HashMap<String, String>();
		name.put("name", "phony");
		name.put("password", "s3cr3t");
		name.put("email", "phony@phony.com");
		name.put("salt", "s4lt");
		rs.setNextStrings(name);
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
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testLoadQuestions() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testLoadQuestionResultDates() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testLoadQuestionResults() {
		Assert.fail("Not yet implemented");
	}

}
