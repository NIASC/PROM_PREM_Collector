package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.MapData;

public class RequestLogoutTest {
	RequestLogout processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		processer = new RequestLogout(dbutil.um, dbutil.db, dbutil.pd, dbutil.logger, dbutil.crypto);
	}

	@Test
	public void testProcessRequestLoggedIn() {
		String insert = processRequest(requtil.login());
        Assert.assertEquals(Packet.SUCCESS, insert);
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
		String insert = processRequest(0L);
        Assert.assertEquals(Packet.ERROR, insert);
	}

	public String processRequest(long uid) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.REQ_LOGOUT);
        MapData dataOut = dbutil.pd.getMapData();

		MapData details = dbutil.pd.getMapData();
        details.put(Packet.UID, Long.toString(uid));
        dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));

        out.put(Packet.DATA, dataOut.toString());
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return inData.get(Packet.RESPONSE);
        } catch (NumberFormatException ignored) {
            return Packet.ERROR;
        }
	}

}
