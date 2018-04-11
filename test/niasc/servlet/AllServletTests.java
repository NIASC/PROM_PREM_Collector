package niasc.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.core.AllCoreTests;
import niasc.servlet.implementation.AllImplementationTests;

@RunWith(Suite.class)
@SuiteClasses({ AllCoreTests.class, AllImplementationTests.class })
public class AllServletTests {
}
