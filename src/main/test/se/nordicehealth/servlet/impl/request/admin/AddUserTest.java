package se.nordicehealth.servlet.impl.request.admin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
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
		details.put(AdminPacket.AdminData.AdminAddUser.Details.CLINIC_ID, 1);
		details.put(AdminPacket.AdminData.AdminAddUser.Details.EMAIL, "example@phony.com");
		details.put(AdminPacket.AdminData.AdminAddUser.Details.NAME, "phony name");
		details.put(AdminPacket.AdminData.AdminAddUser.Details.PASSWORD, "s3cr3t");
		details.put(AdminPacket.AdminData.AdminAddUser.Details.SALT, "s4lt");
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_USER);
		MapData data = dbutil.pd.getMapData();
		// default details
		data.put(AdminPacket.AdminData.AdminAddUser.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);

		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_USER,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddUser.Response.SUCCESS,
				Constants.getEnum(AdminData.AdminAddUser.Response.values(), response.get(AdminData.AdminAddUser.RESPONSE))));
	}

	@Test
	public void testProcessRequestInvalidEmail() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.AdminData.AdminAddUser.Details.EMAIL, "phony.com");
		data.put(AdminPacket.AdminData.AdminAddUser.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_USER,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddUser.Response.FAIL,
				Constants.getEnum(AdminData.AdminAddUser.Response.values(), response.get(AdminData.AdminAddUser.RESPONSE))));
	}

	@Test
	public void testProcessRequestInvalidClinic() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.AdminData.AdminAddUser.Details.CLINIC_ID, 2);
		data.put(AdminPacket.AdminData.AdminAddUser.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_USER,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddUser.Response.FAIL,
				Constants.getEnum(AdminData.AdminAddUser.Response.values(), response.get(AdminData.AdminAddUser.RESPONSE))));
	}

	@Test
	public void testProcessRequestEmptyName() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.ADD_USER);
		MapData data = dbutil.pd.getMapData();
		details.put(AdminPacket.AdminData.AdminAddUser.Details.NAME, " ");
		data.put(AdminPacket.AdminData.AdminAddUser.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.ADD_USER,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminAddUser.Response.FAIL,
				Constants.getEnum(AdminData.AdminAddUser.Response.values(), response.get(AdminData.AdminAddUser.RESPONSE))));
	}

}
