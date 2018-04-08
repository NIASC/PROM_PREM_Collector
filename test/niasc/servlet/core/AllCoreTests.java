package niasc.servlet.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.core.statistics.AllStatisticsTests;
import niasc.servlet.core.usermanager.AllUsermanagerTests;

@RunWith(Suite.class)
@SuiteClasses({ AllStatisticsTests.class, AllUsermanagerTests.class })
public class AllCoreTests {
}
