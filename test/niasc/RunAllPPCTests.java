package niasc;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.AllServletTests;

@RunWith(Suite.class)
@SuiteClasses({ AllServletTests.class })
public class RunAllPPCTests {
}