package niasc.servlet.core.statistics.containers;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.statistics.containers.SingleOption;

public class SingleOptionTest {
	SingleOption so;
	int id = 1;
	int option1 = 1;

	@Before
	public void setUp() throws Exception {
		so = new SingleOption(id, option1);
	}

	@Test
	public void testQuestion() {
		Assert.assertEquals(id, so.question());
	}

	@Test
	public void testAnswerIdentifierAndText() {
		List<Object> l = so.answerIdentifierAndText();
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(true, l.get(0) instanceof Integer);
		Assert.assertEquals(option1, ((Integer) l.get(0)).intValue());
		try {
			Integer i = (Integer) l.get(1);
			fail("Index 1 should throw OutOfBoundsException but it returned '"+i+"'");
		} catch (IndexOutOfBoundsException ignored) { }
		try {
			l.add(10);
			fail("List should not be modifiable");
		} catch (UnsupportedOperationException ignored) { }
	}

}
