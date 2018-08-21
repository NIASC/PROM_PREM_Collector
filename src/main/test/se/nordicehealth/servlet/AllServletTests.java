package se.nordicehealth.servlet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.core.AllCoreTests;
import se.nordicehealth.servlet.impl.AllImplementationTests;

@RunWith(Suite.class)
@SuiteClasses({ AllCoreTests.class, AllImplementationTests.class })
public class AllServletTests {
}
