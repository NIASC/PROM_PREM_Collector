package niasc.servlet.core.usermanager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.implementation.Packet.Data.RequestLogin.Response;
import servlet.core.usermanager.ConnectionManager;
import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.ThreadedActivityMonitor;
import servlet.core.usermanager.UserManager;

public class UserManagerTest {
	UserManager um;
	String identifier1 = "name1", identifier2 = "name2", identifier3 = "name3";
	long uid1 = 111L, uid2 = 222L, uid3 = 333L;
	
	private final int cyclesBeforeIdle = 10;
	private final int cyclesBeforeInactive = (5*cyclesBeforeIdle)/2;
	private final long millisPerCycle = 10;

	@Before
	public void setUp() throws Exception {
		ConnectionManager cm = new RegisteredOnlineUserManager();
		um = new UserManager(cm, new ThreadedActivityMonitor(cm, millisPerCycle, cyclesBeforeIdle, cyclesBeforeInactive), 2);
		um.startManagement();
	}

	@After
	public void tearDown() throws Exception {
		um.stopManagement();
	}

	@Test
	public void testAddUserToListOfOnline() {
		Assert.assertEquals(false, um.isOnline(uid1));
		Assert.assertEquals(Response.SUCCESS, um.addUserToListOfOnline(identifier1, uid1));
		Assert.assertEquals(true, um.isOnline(uid1));
		Assert.assertEquals(Response.ERROR, um.addUserToListOfOnline(null, uid1));
		Assert.assertEquals(Response.ERROR, um.addUserToListOfOnline("", uid1));
		Assert.assertEquals(Response.ERROR, um.addUserToListOfOnline(identifier2, uid1));
		Assert.assertEquals(Response.ALREADY_ONLINE, um.addUserToListOfOnline(identifier1, uid2));

		Assert.assertEquals(Response.SUCCESS, um.addUserToListOfOnline(identifier2, uid2));
		Assert.assertEquals(Response.SERVER_FULL, um.addUserToListOfOnline(identifier3, uid3));
	}

	@Test
	public void testDelUserFromListOfOnline() {
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(true, um.isOnline(uid1));
		um.delUserFromListOfOnline(uid1);
		Assert.assertEquals(false, um.isOnline(uid1));
	}

	@Test
	public void testIdleKicksUser() {
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(true, um.isOnline(uid1));
		try { Thread.sleep(millisPerCycle*(cyclesBeforeIdle+1)); } catch (Exception e) { }
		Assert.assertEquals(false, um.isOnline(uid1));
	}

	@Test
	public void testInactivityKicksUser() {
		um.addUserToListOfOnline(identifier1, uid1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle; i > 0; --i) {
			Assert.assertEquals(true, um.isOnline(uid1));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			um.refreshIdleTimer(uid1);
		}
		try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
		Assert.assertEquals(false, um.isOnline(uid1));
	}

	@Test
	public void testRefreshIdleKeepsUser() {
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(true, um.isOnline(uid1));
		try { Thread.sleep(millisPerCycle*(cyclesBeforeIdle-1)); } catch (Exception e) { }
		Assert.assertEquals(true, um.isOnline(uid1));
		um.refreshIdleTimer(uid1);
		try { Thread.sleep(millisPerCycle*2); } catch (Exception e) { }
		Assert.assertEquals(true, um.isOnline(uid1));
	}

	@Test
	public void testRefreshInactivityKeepsUser() {
		um.addUserToListOfOnline(identifier1, uid1);
		for (int i = cyclesBeforeInactive/cyclesBeforeIdle-1; i > 0; --i) {
			Assert.assertEquals(true, um.isOnline(uid1));
			try { Thread.sleep(millisPerCycle*cyclesBeforeIdle); } catch (Exception e) { }
			um.refreshIdleTimer(uid1);
		}
		Assert.assertEquals(true, um.isOnline(uid1));
		try { Thread.sleep(millisPerCycle*(cyclesBeforeIdle-1)); } catch (Exception e) { }
		um.refreshInactivityTimer(uid1);
		Assert.assertEquals(true, um.isOnline(uid1));
	}

	@Test
	public void testNameForUID() {
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(identifier1, um.nameForUID(uid1));
	}

	@Test
	public void testIsOnline() {
		Assert.assertEquals(false, um.isOnline(uid1));
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(true, um.isOnline(uid1));
		Assert.assertEquals(false, um.isOnline(uid2));
	}

	@Test
	public void testIsAvailable() {
		Assert.assertEquals(true, um.isAvailable(uid1));
		um.addUserToListOfOnline(identifier1, uid1);
		Assert.assertEquals(false, um.isAvailable(uid1));
		Assert.assertEquals(true, um.isAvailable(uid2));
	}

}
