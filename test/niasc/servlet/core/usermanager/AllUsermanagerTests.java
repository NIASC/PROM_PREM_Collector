package niasc.servlet.core.usermanager;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ConnectionDataTest.class, RegisteredOnlineUserManagerTest.class, ThreadedActivityMonitorTest.class, UserManagerTest.class })
public class AllUsermanagerTests {
}
