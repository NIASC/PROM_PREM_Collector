package se.nordicehealth.servlet.impl.request.admin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	AddClinicTest.class, AddUserTest.class, GetClinicsTest.class,
	GetUserTest.class, RespondRegistrationTest.class
})
public class AllAdminTests {
}
