package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
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
			public String newPassError(User user, String oldPass, String newPass1, String newPass2) {
				return newPass1.equals(newPass2) ? Packet.SUCCESS : Packet.MISMATCH_NEW;
			}
		};
		processer = new SetPassword(dbutil.um, dbutil.db, dbutil.pd, dbutil.logger, dbutil.encryption, dbutil.crypto, validation);
	}
	
	private MapData createDetails(long uid, String oldPass, String newPass1, String newPass2) {
		MapData details = dbutil.pd.getMapData();
        details.put(Packet.UID, Long.toString(uid));
        details.put(Packet.OLD_PASSWORD, oldPass);
        details.put(Packet.NEW_PASSWORD1, newPass1);
        details.put(Packet.NEW_PASSWORD2, newPass2);
        return details;
	}

	@Test
	public void testProcessRequestLoggedIn() {
		long uid = requtil.login();
        String respone = processRequest(uid);
        Assert.assertEquals(Packet.SUCCESS, respone);
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
		long uid = 0L;
        String respone = processRequest(uid);
        Assert.assertEquals(Packet.ERROR, respone);
	}
	
	public String processRequest(long uid) {
        MapData dataOut = dbutil.pd.getMapData();
		MapData details = createDetails(uid, "password", "p4ssw0rd", "p4ssw0rd");
        dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));
        return sendRequest(dataOut);
	}

	public String sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.SET_PASSWORD);
        out.put(Packet.DATA, dataOut.toString());
		requtil.setNextDatabaseUserCall();
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return inData.get(Packet.RESPONSE);
        } catch (NumberFormatException ignored) {
            return Packet.ERROR;
        }
	}

}
