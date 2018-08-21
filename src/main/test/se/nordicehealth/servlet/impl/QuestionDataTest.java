package se.nordicehealth.servlet.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.QuestionData;

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
