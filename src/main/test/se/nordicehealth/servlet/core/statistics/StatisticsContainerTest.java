package se.nordicehealth.servlet.core.statistics;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.core.statistics.StatisticsContainer;
import se.nordicehealth.servlet.core.statistics.StatisticsData;
import se.nordicehealth.servlet.core.statistics.containers.Area;

public class StatisticsContainerTest {
	StatisticsContainer sc;

	@Before
	public void setUp() throws Exception {
		sc = new StatisticsContainer();
	}

	@Test
	public void testAddResultAndGetStatistics() {
		int question1 = 1;
		int question2 = 2;
		sc.addResult(new Area(question1, "test"));
		sc.addResult(new Area(question1, "test"));
		sc.addResult(new Area(question1, "test1"));
		sc.addResult(new Area(question2, "test"));
		sc.addResult(new Area(question2, "test1"));
		sc.addResult(null);
		List<StatisticsData> s = sc.getStatistics();
		Assert.assertEquals(2, s.size());
		Assert.assertEquals(question1, s.get(0).getQuestionID());
		Assert.assertEquals(question2, s.get(1).getQuestionID());
		try {
			int i = s.get(2).getQuestionID();
			Assert.fail("StatisticsData should only contain 2 questions but third question id was: '"+i+"'.");
		} catch (IndexOutOfBoundsException ignored) { }
	}

}
