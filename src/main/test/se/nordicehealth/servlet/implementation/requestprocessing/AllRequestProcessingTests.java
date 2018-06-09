package se.nordicehealth.servlet.implementation.requestprocessing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.implementation.requestprocessing.admin.AllAdminTests;

@RunWith(Suite.class)
@SuiteClasses({ QDBFormatTest.class, AllAdminTests.class })
public class AllRequestProcessingTests {
}
