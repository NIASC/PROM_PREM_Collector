package niasc.servlet.core.usermanager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.usermanager.ThreadedActivityMonitor;
import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.ConnectionData;
import servlet.core.usermanager.ConnectionManager;

public class ThreadedActivityMonitorTest {
	ConnectionManager cmgr;
	ConnectionData ud1, ud2;
	ThreadedActivityMonitor acmon;
	private int cyclesBeforeIdle = 10;
	private int cyclesBeforeInactive = (5*cyclesBeforeIdle)/2;
	private long millisPerCycle = 5;

	@Before
	public void setUp() throws Exception {
		ud1 = new ConnectionData("name1", 111L);
		ud2 = new ConnectionData("name2", 222L);
		cmgr = new RegisteredOnlineUserManager();
		acmon = new ThreadedActivityMonitor(cmgr, millisPerCycle, cyclesBeforeIdle, cyclesBeforeInactive);
		acmon.start();
	}

	@After
	public void tearDown() throws Exception {
		acmon.stop();
	}

	@Test
	public void testKickOnIdle() {
		cmgr.registerConnection(ud1);
		Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
		try { Thread.sleep(millisPerCycle*(cyclesBeforeIdle+1)); } catch (Exception e) { }
		Assert.assertEquals(true, ud1.idleGreaterThan(cyclesBeforeIdle-1));
		Assert.assertEquals(false, cmgr.isConnected(ud1.identifier()));
	}
	
	@Test
	public void testRefreshIdleKeepsUserOnline() {
		cmgr.registerConnection(ud1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle - 1; i > 0; --i) {
			Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			cmgr.refreshIdleTimer(ud1.UID());
		}
		Assert.assertEquals(false, ud1.inactiveGreaterThan(cyclesBeforeIdle));
		Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
	}

	@Test
	public void testKickOnInactive() {
		cmgr.registerConnection(ud1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle + 1; i > 0; --i) {
			Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			cmgr.refreshIdleTimer(ud1.UID());
		}
		Assert.assertEquals(true, ud1.inactiveGreaterThan(cyclesBeforeInactive));
		Assert.assertEquals(false, cmgr.isConnected(ud1.identifier()));
	}
	
	@Test
	public void testRefreshInactiveKeepsUserOnline() {
		cmgr.registerConnection(ud1);
		for (int i = 0; i < 2; ++i) {
			for (int j = cyclesBeforeInactive/cyclesBeforeIdle - 1; j > 0; --j) {
				Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
				try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
				cmgr.refreshIdleTimer(ud1.UID());
			}
			cmgr.refreshInactivityTimer(ud1.UID());
		}
		Assert.assertEquals(false, ud1.inactiveGreaterThan(cyclesBeforeInactive));
		Assert.assertEquals(true, cmgr.isConnected(ud1.identifier()));
	}
}
