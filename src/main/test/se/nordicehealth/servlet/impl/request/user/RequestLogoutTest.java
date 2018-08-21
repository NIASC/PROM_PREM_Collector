package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
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
		Packet.Data.RequestLogout.Response insert = processRequest(requtil.login());
        Assert.assertTrue(Constants.equal(Packet.Data.RequestLogout.Response.SUCCESS, insert));
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
		Packet.Data.RequestLogout.Response insert = processRequest(0L);
        Assert.assertTrue(Constants.equal(Packet.Data.RequestLogout.Response.ERROR, insert));
	}

	public Packet.Data.RequestLogout.Response processRequest(long uid) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.REQ_LOGOUT);
        MapData dataOut = dbutil.pd.getMapData();

		MapData details = dbutil.pd.getMapData();
        details.put(Packet.Data.RequestLogout.Details.UID, Long.toString(uid));
        dataOut.put(Packet.Data.RequestLogout.DETAILS, dbutil.crypto.encrypt(details.toString()));

        out.put(Packet.DATA, dataOut.toString());
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return Constants.getEnum(Packet.Data.RequestLogout.Response.values(), inData.get(Packet.Data.RequestLogout.RESPONSE));
        } catch (NumberFormatException ignored) {
            return Packet.Data.RequestLogout.Response.ERROR;
        }
	}

}
