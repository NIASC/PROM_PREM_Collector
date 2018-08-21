package se.nordicehealth.servlet.core.stats.containers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.core.stats.containers.MultipleOption;

public class MultipleOptionTest {
	MultipleOption mo;
	int id = 1;
	int option1 = 1;
	int option2 = 2;
	int option5 = 5;
	List<Integer> options;

	@Before
	public void setUp() throws Exception {
		options = Arrays.asList(option1, option2, option5);
		mo = new MultipleOption(id, options);
	}

	@Test
	public void testQuestion() {
		Assert.assertEquals(id, mo.question());
	}

	@Test
	public void testAnswerIdentifierAndText() {
		List<Object> l = mo.answerIdentifierAndText();
		Assert.assertEquals(options.size(), l.size());
		Assert.assertEquals(true, l.get(0) instanceof Integer);
		Assert.assertEquals(option1, ((Integer) l.get(0)).intValue());
		Assert.assertEquals(option2, ((Integer) l.get(1)).intValue());
		Assert.assertEquals(option5, ((Integer) l.get(2)).intValue());
		try {
			Integer i = (Integer) l.get(3);
			fail("Index 1 should throw OutOfBoundsException but it returned '"+i+"'");
		} catch (IndexOutOfBoundsException ignored) { }
		try {
			l.add(10);
			fail("List should not be modifiable");
		} catch (UnsupportedOperationException ignored) { }
	}

}
