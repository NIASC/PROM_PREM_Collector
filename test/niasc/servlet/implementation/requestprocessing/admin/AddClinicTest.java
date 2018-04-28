package niasc.servlet.implementation.requestprocessing.admin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import common.implementation.Constants;
import servlet.implementation.AdminPacket;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.io.MapData;
import servlet.implementation.requestprocessing.admin._AddClinic;

public class AddClinicTest {
	_AddClinic processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		processer = new _AddClinic(dbutil.pd, dbutil.logger, dbutil.db);
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_CLINIC);
		MapData data = dbutil.pd.getMapData();
		data.put(AdminData.AdminAddClinic.NAME, "dummy");
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_CLINIC,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddClinic.Response.SUCCESS,
				Constants.getEnum(AdminData.AdminAddClinic.Response.values(), response.get(AdminData.AdminAddClinic.RESPONSE))));
	}

	@Test
	public void testProcessRequestEmptyPacket() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_CLINIC);
		MapData data = dbutil.pd.getMapData();
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(null, dbutil.s.getLastSQLUpdate());
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_CLINIC,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddClinic.Response.FAIL,
				Constants.getEnum(AdminData.AdminAddClinic.Response.values(), response.get(AdminData.AdminAddClinic.RESPONSE))));
	}

}
