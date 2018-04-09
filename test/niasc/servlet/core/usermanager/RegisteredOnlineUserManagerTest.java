package niasc.servlet.core.usermanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.ConnectionData;

public class RegisteredOnlineUserManagerTest {
	RegisteredOnlineUserManager roum;
	ConnectionData ud1, ud2;
	long uid1 = 999L, uid2 = 555L;
	String name1 = "newuser1", name2 = "newuser2";

	@Before
	public void setUp() throws Exception {
		roum = new RegisteredOnlineUserManager();
		ud1 = new ConnectionData(name1, uid1);
		ud2 = new ConnectionData(name2, uid2);
	}
	
	@Test
	public void testRegisterOnlineUser() {
		roum.registerConnection(ud1);
		Assert.assertEquals(true, roum.isConnected(ud1.identifier()));
		Assert.assertEquals(1, roum.registeredConnections());
	}
	
	@Test
	public void testRegisterNullUIDUser() {
		ConnectionData zeroUIDUser = new ConnectionData("zeroUIDUser", 0L);
		roum.registerConnection(zeroUIDUser);
		Assert.assertEquals(false, roum.isConnected(zeroUIDUser.identifier()));
		Assert.assertEquals(0, roum.registeredConnections());
	}
	
	@Test
	public void testRegisterNullNameUser() {
		ConnectionData nullNameUser = new ConnectionData(null, 444L);
		roum.registerConnection(nullNameUser);
		Assert.assertEquals(false, roum.isConnected(nullNameUser.UID()));
		Assert.assertEquals(0, roum.registeredConnections());
	}

	@Test
	public void testRegisterNullUser() {
		roum.registerConnection(null);
		Assert.assertEquals(0, roum.registeredConnections());
		
	}

	@Test
	public void testDeregisterOnlineUserUserData() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(2, roum.registeredConnections());
		roum.deregisterConnection(ud1);
		Assert.assertEquals(false, roum.isConnected(ud1.UID()));
		Assert.assertEquals(true, roum.isConnected(ud2.UID()));
		Assert.assertEquals(1, roum.registeredConnections());
	}

	@Test
	public void testDeregisterOnlineUserLong() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(2, roum.registeredConnections());
		roum.deregisterConnection(ud1.UID());
		Assert.assertEquals(false, roum.isConnected(ud1.UID()));
		Assert.assertEquals(true, roum.isConnected(ud2.UID()));
		Assert.assertEquals(1, roum.registeredConnections());
	}

	@Test
	public void testDeregisterAllOnlineUsers() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(2, roum.registeredConnections());
		roum.deregisterAllConnections();
		Assert.assertEquals(false, roum.isConnected(ud1.UID()));
		Assert.assertEquals(false, roum.isConnected(ud2.UID()));
		Assert.assertEquals(0, roum.registeredConnections());
	}

	@Test
	public void testIterable() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		int users = 0;
		for (ConnectionData _ : roum.iterable()) {
			users++;
		}
		Assert.assertEquals(2, users);
	}

	@Test
	public void testRegisteredUsersOnline() {
		Assert.assertEquals(0, roum.registeredConnections());
		roum.registerConnection(ud1);
		Assert.assertEquals(1, roum.registeredConnections());
		roum.registerConnection(ud2);
		Assert.assertEquals(2, roum.registeredConnections());
		roum.registerConnection(null);
		Assert.assertEquals(2, roum.registeredConnections());
		roum.registerConnection(ud1);
		Assert.assertEquals(2, roum.registeredConnections());
	}

	@Test
	public void testIsOnlineString() {
		roum.registerConnection(ud1);
		Assert.assertEquals(true, roum.isConnected(ud1.identifier()));
		Assert.assertEquals(false, roum.isConnected(ud2.identifier()));
		Assert.assertEquals(false, roum.isConnected(null));
	}

	@Test
	public void testIsOnlineLong() {
		roum.registerConnection(ud1);
		Assert.assertEquals(true, roum.isConnected(ud1.UID()));
		Assert.assertEquals(false, roum.isConnected(ud2.UID()));
		Assert.assertEquals(false, roum.isConnected(0L));
	}

	@Test
	public void testGetUserString() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(ud1.identifier(), roum.getConnection(ud1.identifier()).identifier());
		Assert.assertNotEquals(ud1.identifier(), roum.getConnection(ud2.identifier()).identifier());
		Assert.assertNull(roum.getConnection(null));
	}

	@Test
	public void testGetUserLong() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(ud1.UID(), roum.getConnection(ud1.UID()).UID());
		Assert.assertNotEquals(ud1.UID(), roum.getConnection(ud2.UID()).UID());
		Assert.assertNull(roum.getConnection(0L));
	}

	@Test
	public void testNameForUID() {
		roum.registerConnection(ud1);
		Assert.assertEquals(ud1.identifier(), roum.identifierForUID(ud1.UID()));
		Assert.assertNull(roum.identifierForUID(ud2.UID()));
		Assert.assertNull(roum.identifierForUID(0L));
	}

	@Test
	public void testRefreshInactivityTimer() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(false, ud1.inactiveGreaterThan(0));
		Assert.assertEquals(false, ud2.inactiveGreaterThan(0));
		ud1.tickInactive();
		Assert.assertEquals(true, ud1.inactiveGreaterThan(0));
		Assert.assertEquals(false, ud2.inactiveGreaterThan(0));
		roum.refreshInactivityTimer(ud1.UID());
		Assert.assertEquals(false, ud1.inactiveGreaterThan(0));
		Assert.assertEquals(false, ud2.inactiveGreaterThan(0));
	}

	@Test
	public void testRefreshIdleTimer() {
		roum.registerConnection(ud1);
		roum.registerConnection(ud2);
		Assert.assertEquals(false, ud1.idleGreaterThan(0));
		Assert.assertEquals(false, ud2.idleGreaterThan(0));
		ud1.tickIdle();
		Assert.assertEquals(true, ud1.idleGreaterThan(0));
		Assert.assertEquals(false, ud2.idleGreaterThan(0));
		roum.refreshIdleTimer(ud1.UID());
		Assert.assertEquals(false, ud1.idleGreaterThan(0));
		Assert.assertEquals(false, ud2.idleGreaterThan(0));
	}

}
