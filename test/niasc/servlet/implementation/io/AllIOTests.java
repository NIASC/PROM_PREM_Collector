package niasc.servlet.implementation.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ListDataTest.class, MapDataTest.class, PacketDataTest.class })
public class AllIOTests {
}
