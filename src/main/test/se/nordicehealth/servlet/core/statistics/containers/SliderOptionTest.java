package se.nordicehealth.servlet.core.statistics.containers;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.core.statistics.containers.Slider;

public class SliderOptionTest {
	Slider s;
	int id = 1;
	int value = 1;

	@Before
	public void setUp() throws Exception {
		s = new Slider(id, value);
	}

	@Test
	public void testQuestion() {
		Assert.assertEquals(id, s.question());
	}

	@Test
	public void testAnswerIdentifierAndText() {
		List<Object> l = s.answerIdentifierAndText();
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(true, l.get(0) instanceof Integer);
		Assert.assertEquals(value, ((Integer) l.get(0)).intValue());
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
