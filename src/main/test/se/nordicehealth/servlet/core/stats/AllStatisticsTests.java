package se.nordicehealth.servlet.core.stats;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.core.stats.containers.AllContainerTests;

@RunWith(Suite.class)
@SuiteClasses({ StatementOccurrenceTest.class, StatisticsContainerTest.class, StatisticsContainerTest.class, StatisticsDataTest.class, AllContainerTests.class })
public class AllStatisticsTests {
}
