package se.nordicehealth.servlet.implementation.requestprocessing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.implementation.requestprocessing.admin.AllAdminTests;
import se.nordicehealth.servlet.implementation.requestprocessing.user.AllUserTests;

@RunWith(Suite.class)
@SuiteClasses({ QDBFormatTest.class, AllAdminTests.class, AllUserTests.class })
public class AllRequestProcessingTests {
}
