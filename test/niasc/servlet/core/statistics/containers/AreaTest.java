package niasc.servlet.core.statistics.containers;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import servlet.core.statistics.containers.Area;

public class AreaTest {
	Area a;
	int id = 1;
	String text = "entry";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		a = new Area(id, text);
	}

	@Test
	public void testQuestion() {
		Assert.assertEquals(id, a.question());
	}

	@Test
	public void testAnswerIdentifierAndText() {
		List<Object> l = a.answerIdentifierAndText();
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(true, l.get(0) instanceof String);
		Assert.assertEquals(text, (String) l.get(0));
		try {
			String s = (String) l.get(1);
			fail("Index 1 should throw OutOfBoundsException but it returned '"+s+"'");
		} catch (IndexOutOfBoundsException ignored) { }
		try {
			l.add("a new entry");
			fail("List should not be modifiable");
		} catch (UnsupportedOperationException ignored) { }

	}

}
