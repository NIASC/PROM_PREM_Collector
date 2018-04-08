package niasc.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.core.AllCoreTests;

@RunWith(Suite.class)
@SuiteClasses({ AllCoreTests.class })
public class AllServletTests {
}
