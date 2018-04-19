package niasc.servlet.implementation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.implementation.Packet;
import common.implementation.Packet.Types;
import niasc.phony.PhonyDatabase;
import niasc.servlet.LoggerForTesting;
import servlet.core.PPCDatabase;
import servlet.core.PPCLogger;
import servlet.core.usermanager.ActivityMonitor;
import servlet.core.usermanager.ConnectionData;
import servlet.core.usermanager.ConnectionManager;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.ClientRequestProcesser;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

public class ClientRequestProcesserTest {
	ClientRequestProcesser crp;
	PacketData pd;
	LoggerForTesting logger;
	MapData userPacket, adminPacket, adminUserPacket;
	MapData userData, adminData, adminUserData;
	
	private class DummyRequestProcesser extends RequestProcesser {
		public DummyRequestProcesser(PPCDatabase db, IPacketData packetData, PPCLogger logger) {
			super(packetData, logger);
		}
		@Override
		public MapData processRequest(MapData in) throws Exception {
			return in;
		}
	}

	@Before
	public void setUp() throws Exception {
		logger = new LoggerForTesting();
		pd = new PacketData(new JSONParser(), logger);
		userPacket = pd.getMapData();
		userPacket.put(Packet.TYPE, Packet.Types.PING);
		userData = pd.getMapData();
		userData.put(Packet.Data.Ping.__NULL__, "userData");
		userPacket.put(Packet.DATA, userData.toString());
		adminPacket = pd.getMapData();
		adminPacket.put(AdminPacket._TYPE, AdminPacket.AdminTypes.GET_USER);
		adminPacket.put(AdminPacket._ADMIN, AdminPacket.Admin.YES);
		adminData = pd.getMapData();
		adminData.put(AdminPacket.AdminData.AdminGetUser.__NULL__, "adminData");
		adminPacket.put(AdminPacket._DATA, adminData.toString());
		adminUserPacket = pd.getMapData();
		adminUserPacket.put(AdminPacket._TYPE, AdminPacket.AdminTypes.GET_USER);
		adminUserPacket.put(AdminPacket._ADMIN, AdminPacket.Admin.NO);
		adminUserData = pd.getMapData();
		adminUserData.put(AdminPacket.AdminData.AdminGetUser.__NULL__, "adminUserData");
		adminUserPacket.put(AdminPacket._DATA, adminUserData.toString());
		
		PPCDatabase db = new PhonyDatabase();
		ConnectionManager cmgr = new ConnectionManager() {
			@Override public void registerConnection(ConnectionData connection) { }
			@Override public void deregisterConnection(ConnectionData connection) { }
			@Override public void deregisterConnection(long uid) { }
			@Override public void deregisterAllConnections() { }
			@Override public Collection<ConnectionData> iterable() { return null; }
			@Override public int registeredConnections() { return 0; }
			@Override public boolean isConnected(String identifier) { return false; }
			@Override public boolean isConnected(long uid) { return false; }
			@Override public ConnectionData getConnection(String identifier) { return null; }
			@Override public ConnectionData getConnection(long uid) { return null; }
			@Override public String identifierForUID(long uid) { return null; }
			@Override public boolean refreshInactivityTimer(long uid) { return false; }
			@Override public boolean refreshIdleTimer(long uid) { return false; }
		};
		ActivityMonitor acmon = new ActivityMonitor() {
			@Override public void start() { }
			@Override public void stop() { }
		};
		UserManager um = new UserManager(cmgr, acmon);
		Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
		RequestProcesser urp = new DummyRequestProcesser(db, pd, logger);
		userMethods.put(Packet.Types.PING, urp);
		Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
		adminMethods.put(AdminPacket.AdminTypes.GET_USER, urp);
		crp = new ClientRequestProcesser(logger, pd, um, userMethods, adminMethods);
	}

	@Test
	public void testHandleUserRequestDifferentIP() {
		String in = userPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.2");
		Assert.assertEquals(in, out);
	}

	@Test
	public void testHandleUserRequestSameIP() {
		String in = userPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.1");
		Assert.assertEquals(in, out);
	}

	@Test
	public void testHandleAdminRequestSameIP() {
		String in = adminPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.1");
		Assert.assertEquals(in, out);
	}

	@Test
	public void testHandleAdminRequestDifferentIP() {
		String in = adminPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.2");
		Assert.assertEquals(pd.getMapData().toString(), out);
	}

	@Test
	public void testHandleAdminUserRequestSameIP() {
		String in = adminUserPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.1");
		Assert.assertEquals(in, out);
	}

	@Test
	public void testHandleAdminUserRequestDifferentIP() {
		String in = adminUserPacket.toString();
		String out = crp.handleRequest(in, "127.0.0.1", "127.0.0.2");
		Assert.assertEquals(in, out);
	}
	
	@Test
	public void testHandleMalformedPacket() {
		Assert.assertEquals(pd.getMapData().toString(), crp.handleRequest("a non-json string", "127.0.0.1", "127.0.0.2"));
		Assert.assertEquals(pd.getMapData().toString(), crp.handleRequest(null, "127.0.0.1", "127.0.0.2"));
	}

}
