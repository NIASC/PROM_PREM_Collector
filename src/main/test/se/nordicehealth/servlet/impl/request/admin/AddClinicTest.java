package se.nordicehealth.servlet.impl.request.admin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.AdminPacket;
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
		in.put(AdminPacket._TYPE, AdminPacket._ADD_CLINIC);
		MapData data = dbutil.pd.getMapData();
		data.put(AdminPacket.NAME, "dummy");
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._ADD_CLINIC, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.SUCCESS, response.get(AdminPacket.RESPONSE));
	}

	@Test
	public void testProcessRequestEmptyPacket() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._ADD_CLINIC);
		MapData data = dbutil.pd.getMapData();
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(null, dbutil.s.getLastSQLUpdate());
		Assert.assertEquals(AdminPacket._ADD_CLINIC, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.FAIL, response.get(AdminPacket.RESPONSE));
	}

}
