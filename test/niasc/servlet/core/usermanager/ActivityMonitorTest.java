package niasc.servlet.core.usermanager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.usermanager.ActivityMonitor;
import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.UserData;

public class ActivityMonitorTest {
	RegisteredOnlineUserManager roum;
	UserData ud1, ud2;
	ActivityMonitor am;
	private int cyclesBeforeIdle = 5;
	private int cyclesBeforeInactive = 10;
	private long millisPerCycle = 5;

	@Before
	public void setUp() throws Exception {
		ud1 = new UserData("name1", 111L);
		ud2 = new UserData("name2", 222L);
		roum = new RegisteredOnlineUserManager();
		am = new ActivityMonitor(roum, millisPerCycle, cyclesBeforeIdle, cyclesBeforeInactive);
	}

	@After
	public void tearDown() throws Exception {
		am.stop();
	}

	@Test
	public void testKickOnIdle() {
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(true, roum.isOnline(ud1.Name()));
		try { Thread.sleep(millisPerCycle*(cyclesBeforeIdle+1)); } catch (Exception e) { }
		Assert.assertEquals(true, ud1.idleGreaterThan(cyclesBeforeIdle));
		Assert.assertEquals(false, roum.isOnline(ud1.Name()));
	}
	
	@Test
	public void testRefreshIdleKeepsUserOnline() {
		roum.registerOnlineUser(ud1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle - 1; i > 0; --i) {
			Assert.assertEquals(true, roum.isOnline(ud1.Name()));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			roum.refreshIdleTimer(ud1.UID());
		}
		Assert.assertEquals(false, ud1.inactiveGreaterThan(cyclesBeforeIdle));
		Assert.assertEquals(true, roum.isOnline(ud1.Name()));
	}

	@Test
	public void testKickOnInactive() {
		roum.registerOnlineUser(ud1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle + 1; i > 0; --i) {
			Assert.assertEquals(true, roum.isOnline(ud1.Name()));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			roum.refreshIdleTimer(ud1.UID());
		}
		Assert.assertEquals(true, ud1.inactiveGreaterThan(cyclesBeforeInactive));
		Assert.assertEquals(false, roum.isOnline(ud1.Name()));
	}
	
	@Test
	public void testRefreshInactiveKeepsUserOnline() {
		roum.registerOnlineUser(ud1);
		for (int i = 0; i < 2; ++i) {
			for (int j = cyclesBeforeInactive/cyclesBeforeIdle - 1; j > 0; --j) {
				Assert.assertEquals(true, roum.isOnline(ud1.Name()));
				try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
				roum.refreshIdleTimer(ud1.UID());
			}
			roum.refreshInactivityTimer(ud1.UID());
		}
		Assert.assertEquals(false, ud1.inactiveGreaterThan(cyclesBeforeInactive));
		Assert.assertEquals(true, roum.isOnline(ud1.Name()));
	}
}
