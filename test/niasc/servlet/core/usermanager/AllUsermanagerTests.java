package niasc.servlet.core.usermanager;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UserDataTest.class, RegisteredOnlineUserManagerTest.class, ActivityMonitorTest.class })
public class AllUsermanagerTests {
}
