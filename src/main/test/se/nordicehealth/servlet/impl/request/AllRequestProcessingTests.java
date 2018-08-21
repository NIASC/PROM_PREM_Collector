package se.nordicehealth.servlet.impl.request;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import se.nordicehealth.servlet.impl.request.admin.AllAdminTests;
import se.nordicehealth.servlet.impl.request.user.AllUserTests;

@RunWith(Suite.class)
@SuiteClasses({ QDBFormatTest.class, AllAdminTests.class, AllUserTests.class })
public class AllRequestProcessingTests {
}
