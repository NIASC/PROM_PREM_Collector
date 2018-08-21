package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.user.RequestLogin;

public class RequestLoginTest {
	RequestLogin processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		processer = new RequestLogin(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.encryption, dbutil.crypto);
	}

	@Test
	public void testProcessRequest() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.REQ_LOGIN);
		
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.Data.RequestLogin.DETAILS, dbutil.crypto.encrypt(requtil.createDetailsEntry().toString()));
		out.put(Packet.DATA, dataOut.toString());
		
		requtil.setNextDatabaseUserCall();
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		Assert.assertTrue(Constants.equal(Data.RequestLogin.Response.SUCCESS,
				Constants.getEnum(Data.RequestLogin.Response.values(), inData.get(Data.RequestLogin.RESPONSE))));
	}

	@Test
	public void testProcessRequestNoUIDAvailable() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.REQ_LOGIN);
		
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.Data.RequestLogin.DETAILS, dbutil.crypto.encrypt(requtil.createDetailsEntry().toString()));
		out.put(Packet.DATA, dataOut.toString());
		
		dbutil.um.setIsAvailableUID(false);
		requtil.setNextDatabaseUserCall();
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		Assert.assertTrue(Constants.equal(Data.RequestLogin.Response.FAIL,
				Constants.getEnum(Data.RequestLogin.Response.values(), inData.get(Data.RequestLogin.RESPONSE))));
		Assert.assertEquals(0L, Long.parseLong(inData.get(Data.RequestLogin.UID)));
	}

}
