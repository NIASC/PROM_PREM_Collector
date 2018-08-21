package se.nordicehealth.servlet.impl.request.user;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddQuestionnaireAnswerTest.class, LoadQResultDatesTest.class, LoadQResultsTest.class,
	LoadQuestionsTest.class, PingTest.class, RequestLoginTest.class, RequestLogoutTest.class,
	RequestRegistrationTest.class
})
public class AllUserTests {
}
