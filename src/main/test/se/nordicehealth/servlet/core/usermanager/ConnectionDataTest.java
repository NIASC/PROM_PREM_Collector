package se.nordicehealth.servlet.core.usermanager;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.core.usermanager.ConnectionData;

public class ConnectionDataTest {
	ConnectionData ud;
	long uid = 999L;
	String name = "newuser";

	@Before
	public void setUp() throws Exception {
		ud = new ConnectionData(name, uid);
	}

	@Test
	public void testUID() {
		Assert.assertEquals(uid, ud.UID());
	}

	@Test
	public void testName() {
		Assert.assertEquals(name, ud.identifier());
	}

	@Test
	public void testTickAll() {
		Assert.assertEquals(true, ud.idleGreaterThan(-1));
		Assert.assertEquals(false, ud.idleGreaterThan(0));
		Assert.assertEquals(true, ud.inactiveGreaterThan(-1));
		Assert.assertEquals(false, ud.inactiveGreaterThan(0));
		ud.tickAll();
		Assert.assertEquals(true, ud.idleGreaterThan(0));
		Assert.assertEquals(false, ud.idleGreaterThan(1));
		Assert.assertEquals(true, ud.inactiveGreaterThan(0));
		Assert.assertEquals(false, ud.inactiveGreaterThan(1));
		ud.tickAll();
		ud.tickAll();
		ud.tickAll();
		ud.tickAll();
		ud.tickAll();
		Assert.assertEquals(true, ud.idleGreaterThan(5));
		Assert.assertEquals(false, ud.idleGreaterThan(6));
		Assert.assertEquals(true, ud.inactiveGreaterThan(5));
		Assert.assertEquals(false, ud.inactiveGreaterThan(6));
	}

	@Test
	public void testRefreshAll() {
		ud.tickAll();
		Assert.assertEquals(true, ud.idleGreaterThan(0));
		Assert.assertEquals(true, ud.inactiveGreaterThan(0));
		ud.refreshAll();
		Assert.assertEquals(false, ud.idleGreaterThan(0));
		Assert.assertEquals(false, ud.inactiveGreaterThan(0));
	}

	@Test
	public void testTickAndRefreshIdle() {
		Assert.assertEquals(false, ud.idleGreaterThan(0));
		ud.tickIdle();
		Assert.assertEquals(true, ud.idleGreaterThan(0));
		ud.refreshIdle();
		Assert.assertEquals(false, ud.idleGreaterThan(0));
	}

	@Test
	public void testTickAndRefreshInactive() {
		Assert.assertEquals(false, ud.inactiveGreaterThan(0));
		ud.tickInactive();
		Assert.assertEquals(true, ud.inactiveGreaterThan(0));
		ud.refreshInactive();
		Assert.assertEquals(false, ud.inactiveGreaterThan(0));
	}

}
