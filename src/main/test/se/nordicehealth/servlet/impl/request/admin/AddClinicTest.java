package se.nordicehealth.servlet.impl.request.admin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.admin._AddClinic;

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
