package se.nordicehealth.servlet.impl.request.admin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.admin._AddUser;

public class AddUserTest {
	_AddUser processer;
	ReqProcUtil dbutil;
	MapData details;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		processer = new _AddUser(dbutil.pd, dbutil.logger, dbutil.db);

		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("id", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNumberOfAvailableNextCalls(1);

		details = dbutil.pd.getMapData();
		details.put(AdminPacket.CLINIC_ID, 1);
		details.put(AdminPacket.EMAIL, "example@phony.com");
		details.put(AdminPacket.NAME, "phony name");
		details.put(AdminPacket.PASSWORD, "s3cr3t");
		details.put(AdminPacket.SALT, "s4lt");
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._ADD_USER);
		MapData data = dbutil.pd.getMapData();
		// default details
		data.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);

		Assert.assertEquals(AdminPacket._ADD_USER, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.SUCCESS, response.get(AdminPacket.RESPONSE));
	}

	@Test
	public void testProcessRequestInvalidEmail() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.EMAIL, "phony.com");
		data.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._ADD_USER, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.FAIL, response.get(AdminPacket.RESPONSE));
	}

	@Test
	public void testProcessRequestInvalidClinic() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.CLINIC_ID, 2);
		data.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._ADD_USER, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.FAIL, response.get(AdminPacket.RESPONSE));
	}

	@Test
	public void testProcessRequestEmptyName() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.NAME, " ");
		data.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._ADD_USER, out.get(AdminPacket._TYPE));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.FAIL, response.get(AdminPacket.RESPONSE));
	}

}
