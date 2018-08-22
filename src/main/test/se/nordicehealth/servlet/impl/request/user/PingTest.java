package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.user.Ping;

public class PingTest {
	Ping processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		processer = new Ping(dbutil.pd, dbutil.logger, dbutil.um, dbutil.crypto);
	}

	@Test
	public void testProcessRequest() {
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(requtil.createUserUIDEntry(requtil.login()).toString()));
		String response = sendRequest(dataOut);
		Assert.assertEquals(Packet.SUCCESS, response);
	}

	@Test
	public void testProcessRequestNotOnline() {
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(requtil.createUserUIDEntry(0L).toString()));
		String response = sendRequest(dataOut);
		Assert.assertEquals(Packet.NOT_ONLINE, response);
	}

	public String sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.PING);
		out.put(Packet.DATA, dataOut.toString());

		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return inData.get(Packet.RESPONSE);
        } catch (NumberFormatException ignored) {
        	return Packet.FAIL;
        }
	}

}
