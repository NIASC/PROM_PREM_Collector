package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Packet.Data;
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
		dataOut.put(Packet.Data.Ping.DETAILS, dbutil.crypto.encrypt(requtil.createUserUIDEntry(requtil.login()).toString()));
		Data.Ping.Response response = sendRequest(dataOut);
		Assert.assertTrue(Constants.equal(Data.Ping.Response.SUCCESS, response));
	}

	@Test
	public void testProcessRequestNotOnline() {
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.Data.Ping.DETAILS, dbutil.crypto.encrypt(requtil.createUserUIDEntry(0L).toString()));
		Data.Ping.Response response = sendRequest(dataOut);
		Assert.assertTrue(Constants.equal(Data.Ping.Response.NOT_ONLINE, response));
	}

	public Data.Ping.Response sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.PING);
		out.put(Packet.DATA, dataOut.toString());

		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
        	return Constants.getEnum(Data.Ping.Response.values(), inData.get(Data.Ping.RESPONSE));
        } catch (NumberFormatException ignored) {
        	return Data.Ping.Response.FAIL;
        }
	}

}
