package se.nordicehealth.servlet.impl.mail.emails;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ EMailTest.class, RegistrationRequestTest.class, RegistrationResponseTest.class })
public class AllEMailTests {
}
