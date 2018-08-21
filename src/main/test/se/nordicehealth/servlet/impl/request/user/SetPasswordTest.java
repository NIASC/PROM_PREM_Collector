package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.servlet.core.PPCPasswordValidation;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.MapData;

public class SetPasswordTest {
	SetPassword processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		PPCPasswordValidation validation = new PPCPasswordValidation() {
			@Override
			public Packet.Data.SetPassword.Response newPassError(User user, String oldPass, String newPass1, String newPass2) {
				return newPass1.equals(newPass2) ? Data.SetPassword.Response.SUCCESS : Data.SetPassword.Response.MISMATCH_NEW;
			}
		};
		processer = new SetPassword(dbutil.um, dbutil.db, dbutil.pd, dbutil.logger, dbutil.encryption, dbutil.crypto, validation);
	}
	
	private MapData createDetails(long uid, String oldPass, String newPass1, String newPass2) {
		MapData details = dbutil.pd.getMapData();
        details.put(Data.SetPassword.Details.UID, Long.toString(uid));
        details.put(Data.SetPassword.Details.OLD_PASSWORD, oldPass);
        details.put(Data.SetPassword.Details.NEW_PASSWORD1, newPass1);
        details.put(Data.SetPassword.Details.NEW_PASSWORD2, newPass2);
        return details;
	}

	@Test
	public void testProcessRequestLoggedIn() {
		long uid = requtil.login();
        Data.SetPassword.Response respone = processRequest(uid);
        Assert.assertTrue(Constants.equal(Packet.Data.SetPassword.Response.SUCCESS, respone));
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
		long uid = 0L;
        Data.SetPassword.Response respone = processRequest(uid);
        Assert.assertTrue(Constants.equal(Packet.Data.SetPassword.Response.ERROR, respone));
	}
	
	public Data.SetPassword.Response processRequest(long uid) {
        MapData dataOut = dbutil.pd.getMapData();
		MapData details = createDetails(uid, "password", "p4ssw0rd", "p4ssw0rd");
        dataOut.put(Data.SetPassword.DETAILS, dbutil.crypto.encrypt(details.toString()));
        return sendRequest(dataOut);
	}

	public Data.SetPassword.Response sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.SET_PASSWORD);
        out.put(Packet.DATA, dataOut.toString());
		dbutil.rs.setNumberOfAvailableNextCalls(requtil.setNextDatabaseUserCall("phony", "password"));
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return Constants.getEnum(Data.SetPassword.Response.values(), inData.get(Data.SetPassword.RESPONSE));
        } catch (NumberFormatException ignored) {
            return Data.SetPassword.Response.ERROR;
        }
	}

}
