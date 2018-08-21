package se.nordicehealth.servlet.impl.request.user;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.servlet.impl.io.MapData;

public class UserRequestUtil {
	private ReqProcUtil dbutil;
	RequestLogin processerLogIn;
	
	UserRequestUtil(ReqProcUtil dbutil) {
		this.dbutil = dbutil;
		processerLogIn = new RequestLogin(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.encryption, dbutil.crypto);
	}

	private MapData createDetailsEntry(String username, String password) {
		MapData details = new MapData();
		details.put(Packet.Data.RequestLogin.Details.USERNAME, username);
		details.put(Packet.Data.RequestLogin.Details.PASSWORD, password);
		return details;
	}
	
	public int setNextDatabaseUserCall(String name, String password) {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", name);
		strings.put("password", password);
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		dbutil.rs.setNextStrings(strings);
		return 1;
	}
	
	long login() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.REQ_LOGIN);
		MapData dataOut = dbutil.pd.getMapData();
		// username has to be a number because ReqProcUtil uses PhonyEncryption
		// which does not actually hash the input into a hex string.
		dataOut.put(Packet.Data.RequestLogin.DETAILS, dbutil.crypto.encrypt(createDetailsEntry("12345678", "s3cr3t").toString()));
		out.put(Packet.DATA, dataOut.toString());
		
		dbutil.rs.setNumberOfAvailableNextCalls(setNextDatabaseUserCall("12345678", "s3cr3t"));
		MapData in = processerLogIn.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		Assert.assertTrue(Constants.equal(Data.RequestLogin.Response.SUCCESS,
				Constants.getEnum(Data.RequestLogin.Response.values(), inData.get(Data.RequestLogin.RESPONSE))));
		
		String uid = inData.get(Packet.Data.RequestLogin.UID);
		return uid != null ? Long.parseLong(uid) : 0L;
	}
}
