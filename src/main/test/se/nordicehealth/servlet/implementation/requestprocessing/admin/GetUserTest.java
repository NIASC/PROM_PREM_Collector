package se.nordicehealth.servlet.implementation.requestprocessing.admin;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.implementation.Constants;
import se.nordicehealth.servlet.implementation.AdminPacket;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._GetUser;

public class GetUserTest {
	_GetUser processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		processer = new _GetUser(dbutil.pd, dbutil.logger, dbutil.db);

		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNumberOfAvailableNextCalls(1);
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.GET_USER);
		MapData data = dbutil.pd.getMapData();
		data.put(AdminData.AdminGetUser.USERNAME, "phony");
		in.put(AdminPacket._DATA, data.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.GET_USER,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData data_out = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		MapData user = dbutil.pd.getMapData(data_out.get(AdminData.AdminGetUser.USER));
		
		Assert.assertEquals(0, Integer.parseInt(user.get(AdminData.AdminGetUser.User.CLINIC_ID)));
		Assert.assertEquals("phony", user.get(AdminData.AdminGetUser.User.USERNAME));
		Assert.assertEquals("s3cr3t", user.get(AdminData.AdminGetUser.User.PASSWORD));
		Assert.assertEquals("phony@phony.com", user.get(AdminData.AdminGetUser.User.EMAIL));
		Assert.assertEquals("s4lt", user.get(AdminData.AdminGetUser.User.SALT));
		Assert.assertTrue(Constants.equal(AdminData.AdminGetUser.User.UpdatePassword.YES,
				Constants.getEnum(AdminData.AdminGetUser.User.UpdatePassword.values(), user.get(AdminData.AdminGetUser.User.UPDATE_PASSWORD))));
	}
}
