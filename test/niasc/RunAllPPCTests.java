package niasc;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.core.statistics.AllStatisticsTests;

@RunWith(Suite.class)
@SuiteClasses({ AllStatisticsTests.class })
public class RunAllPPCTests {
}
