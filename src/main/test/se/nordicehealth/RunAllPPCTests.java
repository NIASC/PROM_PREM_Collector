package se.nordicehealth;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.AllServletTests;

@RunWith(Suite.class)
@SuiteClasses({ AllServletTests.class })
public class RunAllPPCTests {
}
