package niasc.servlet.implementation.requestprocessing.admin;

import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.implementation.Constants;
import niasc.phony.database.PhonyConnection;
import niasc.phony.database.PhonyDataSource;
import niasc.phony.database.PhonyResultSet;
import niasc.phony.database.PhonyStatement;
import niasc.servlet.LoggerForTesting;
import servlet.core.PPCLogger;
import servlet.implementation.AdminPacket;
import servlet.implementation.MySQLDatabase;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.admin._AddClinic;

public class AddClinicTest {
	_AddClinic processer;

	IPacketData pd;
	MySQLDatabase db;
	PPCLogger logger;
	
	PhonyDataSource ds;
	PhonyStatement s;
	PhonyResultSet rs;

	@Before
	public void setUp() throws Exception {
		logger = new LoggerForTesting();
		
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		ds = new PhonyDataSource(new PhonyConnection(s));
		db = new MySQLDatabase(ds, null, logger);
		
		pd = new PacketData(new JSONParser(), logger);
		
		processer = new _AddClinic(pd, logger, db);
	}

	@Test
	public void testProcessRequest() {
		MapData in = pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_CLINIC);
		MapData data = pd.getMapData();
		data.put(AdminData.AdminAddClinic.NAME, "dummy");
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertEquals("INSERT INTO `clinics` (`id`, `name`) VALUES (NULL, 'dummy')",
				s.getLastSQLUpdate());
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_CLINIC,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddClinic.Response.SUCCESS,
				Constants.getEnum(AdminData.AdminAddClinic.Response.values(), response.get(AdminData.AdminAddClinic.RESPONSE))));
	}

	@Test
	public void testProcessRequestEmptyPacket() {
		MapData in = pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_CLINIC);
		MapData data = pd.getMapData();
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(null, s.getLastSQLUpdate());
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_CLINIC,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddClinic.Response.FAIL,
				Constants.getEnum(AdminData.AdminAddClinic.Response.values(), response.get(AdminData.AdminAddClinic.RESPONSE))));
	}

}
