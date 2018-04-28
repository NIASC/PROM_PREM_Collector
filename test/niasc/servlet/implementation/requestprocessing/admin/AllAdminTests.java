package niasc.servlet.implementation.requestprocessing.admin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddClinicTest.class, AddUserTest.class, GetClinicsTest.class
})
public class AllAdminTests {
}
