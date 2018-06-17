package se.nordicehealth.servlet.implementation.requestprocessing.user;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddQuestionnaireAnswerTest.class, LoadQResultDatesTest.class, LoadQResultsTest.class,
	LoadQuestionsTest.class, PingTest.class
})
public class AllUserTests {
}
