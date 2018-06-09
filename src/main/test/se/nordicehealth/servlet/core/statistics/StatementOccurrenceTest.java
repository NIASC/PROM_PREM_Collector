package se.nordicehealth.servlet.core.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.core.statistics.StatementOccurrence;
import se.nordicehealth.servlet.core.statistics.containers.Area;

public class StatementOccurrenceTest {
	StatementOccurrence so;
	int question = 4;

	@Before
	public void setUp() throws Exception {
		so = new StatementOccurrence(4);
	}

	@Test
	public void testGetQuestionID() {
		Assert.assertEquals(question, so.getQuestionID());
	}

	@Test
	public void testAddStatementAndGetStatementCount() {
		Assert.assertEquals(0, so.getStatementCount().size());
		so.addAnswer(new Area(question, "test"));
		so.addAnswer(new Area(question, "test"));
		so.addAnswer(new Area(question, "test1"));
		so.addAnswer(new Area(question+1, "test"));
		so.addAnswer(null);
		Assert.assertEquals(2, so.getStatementCount().size());
		List<Integer> m = new ArrayList<Integer>(so.getStatementCount().values());
		Assert.assertEquals(2, m.get(0).intValue());
		Assert.assertEquals(1, m.get(1).intValue());
		try {
			int i = m.get(2);
			Assert.fail("This question has different id and should not have been added, it had a count of '"+i+"'.");
		} catch (IndexOutOfBoundsException ignored) { }
		
	}
}
