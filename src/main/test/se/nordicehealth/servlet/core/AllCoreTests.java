package se.nordicehealth.servlet.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.core.stats.AllStatisticsTests;
import se.nordicehealth.servlet.core.usermanager.AllUsermanagerTests;

@RunWith(Suite.class)
@SuiteClasses({ AllStatisticsTests.class, AllUsermanagerTests.class })
public class AllCoreTests {
}
