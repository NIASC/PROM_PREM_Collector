package niasc.servlet.implementation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.implementation.QuestionData;

public class QuestionDataTest {
	QuestionData qd;

	@Before
	public void setUp() throws Exception {
		qd = new QuestionData();
	}

	@Test
	public void test() {
		Assert.assertNotNull(qd.options);
	}

}
