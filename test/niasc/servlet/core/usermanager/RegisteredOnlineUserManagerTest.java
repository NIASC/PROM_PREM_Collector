package niasc.servlet.core.usermanager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.UserData;

public class RegisteredOnlineUserManagerTest {
	RegisteredOnlineUserManager roum;
	UserData ud1, ud2;
	long uid1 = 999L, uid2 = 555L;
	String name1 = "newuser1", name2 = "newuser2";

	@Before
	public void setUp() throws Exception {
		roum = new RegisteredOnlineUserManager();
		ud1 = new UserData(name1, uid1);
		ud2 = new UserData(name2, uid2);
	}
	
	@Test
	public void testRegisterOnlineUser() {
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(true, roum.isOnline(ud1.Name()));
		Assert.assertEquals(1, roum.registeredUsersOnline());
	}
	
	@Test
	public void testRegisterNullUIDUser() {
		UserData zeroUIDUser = new UserData("zeroUIDUser", 0L);
		roum.registerOnlineUser(zeroUIDUser);
		Assert.assertEquals(false, roum.isOnline(zeroUIDUser.Name()));
		Assert.assertEquals(0, roum.registeredUsersOnline());
	}
	
	@Test
	public void testRegisterNullNameUser() {
		UserData nullNameUser = new UserData(null, 444L);
		roum.registerOnlineUser(nullNameUser);
		Assert.assertEquals(false, roum.isOnline(nullNameUser.UID()));
		Assert.assertEquals(0, roum.registeredUsersOnline());
	}

	@Test
	public void testRegisterNullUser() {
		roum.registerOnlineUser(null);
		Assert.assertEquals(0, roum.registeredUsersOnline());
		
	}

	@Test
	public void testDeregisterOnlineUserUserData() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(2, roum.registeredUsersOnline());
		roum.deregisterOnlineUser(ud1);
		Assert.assertEquals(false, roum.isOnline(ud1.UID()));
		Assert.assertEquals(true, roum.isOnline(ud2.UID()));
		Assert.assertEquals(1, roum.registeredUsersOnline());
	}

	@Test
	public void testDeregisterOnlineUserLong() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(2, roum.registeredUsersOnline());
		roum.deregisterOnlineUser(ud1.UID());
		Assert.assertEquals(false, roum.isOnline(ud1.UID()));
		Assert.assertEquals(true, roum.isOnline(ud2.UID()));
		Assert.assertEquals(1, roum.registeredUsersOnline());
	}

	@Test
	public void testDeregisterAllOnlineUsers() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(2, roum.registeredUsersOnline());
		roum.deregisterAllOnlineUsers();
		Assert.assertEquals(false, roum.isOnline(ud1.UID()));
		Assert.assertEquals(false, roum.isOnline(ud2.UID()));
		Assert.assertEquals(0, roum.registeredUsersOnline());
	}

	@Test
	public void testIterable() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		int users = 0;
		for (UserData _ : roum.iterable()) {
			users++;
		}
		Assert.assertEquals(2, users);
	}

	@Test
	public void testRegisteredUsersOnline() {
		Assert.assertEquals(0, roum.registeredUsersOnline());
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(1, roum.registeredUsersOnline());
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(2, roum.registeredUsersOnline());
		roum.registerOnlineUser(null);
		Assert.assertEquals(2, roum.registeredUsersOnline());
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(2, roum.registeredUsersOnline());
	}

	@Test
	public void testIsOnlineString() {
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(true, roum.isOnline(ud1.Name()));
		Assert.assertEquals(false, roum.isOnline(ud2.Name()));
		Assert.assertEquals(false, roum.isOnline(null));
	}

	@Test
	public void testIsOnlineLong() {
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(true, roum.isOnline(ud1.UID()));
		Assert.assertEquals(false, roum.isOnline(ud2.UID()));
		Assert.assertEquals(false, roum.isOnline(0L));
	}

	@Test
	public void testGetUserString() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(ud1.Name(), roum.getUser(ud1.Name()).Name());
		Assert.assertNotEquals(ud1.Name(), roum.getUser(ud2.Name()).Name());
		Assert.assertNull(roum.getUser(null));
	}

	@Test
	public void testGetUserLong() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
		Assert.assertEquals(ud1.UID(), roum.getUser(ud1.UID()).UID());
		Assert.assertNotEquals(ud1.UID(), roum.getUser(ud2.UID()).UID());
		Assert.assertNull(roum.getUser(0L));
	}

	@Test
	public void testNameForUID() {
		roum.registerOnlineUser(ud1);
		Assert.assertEquals(ud1.Name(), roum.nameForUID(ud1.UID()));
		Assert.assertNull(roum.nameForUID(ud2.UID()));
		Assert.assertNull(roum.nameForUID(0L));
	}

	@Test
	public void testRefreshInactivityTimer() {
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
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
		roum.registerOnlineUser(ud1);
		roum.registerOnlineUser(ud2);
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
