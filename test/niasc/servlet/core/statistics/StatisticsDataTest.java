package niasc.servlet.core.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.statistics.StatementOccurrence;
import servlet.core.statistics.StatisticsData;
import servlet.core.statistics.containers.Area;

public class StatisticsDataTest {
	StatisticsData sc;
	int question1 = 1;

	@Before
	public void setUp() throws Exception {
		StatementOccurrence so1 = new StatementOccurrence(question1);
		so1.addAnswer(new Area(question1, "test"));
		so1.addAnswer(new Area(question1, "test"));
		so1.addAnswer(new Area(question1, "test1"));
		sc = new StatisticsData(so1.getQuestionID(), so1.getStatementCount());
	}
	
	@Test
	public void testGetQuestionID() {
		Assert.assertEquals(question1, sc.getQuestionID());
	}

	@Test
	public void testGetIdentifiersAndCount() {
		List<Integer> l = new ArrayList<Integer>();
		for (Entry<Object, Integer> e : sc.getIdentifiersAndCount()) {
			l.add(e.getValue());
		}
		
		Assert.assertEquals(2, l.get(0).intValue());
		Assert.assertEquals(1, l.get(1).intValue());
		try {
			int i = l.get(2);
			Assert.fail("StatisticsData should only contain 2 questions but third question id was: '"+i+"'.");
		} catch (IndexOutOfBoundsException ignored) { }
	}

}
