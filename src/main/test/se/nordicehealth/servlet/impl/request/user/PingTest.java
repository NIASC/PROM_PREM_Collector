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

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		processer = new Ping(dbutil.pd, dbutil.logger, dbutil.um, dbutil.crypto);
	}

	private MapData createDetailsEntry(long uid) {
		MapData details = new MapData();
		details.put(Packet.Data.LoadQResults.Details.UID, Long.toString(uid));
		return details;
	}

	@Test
	public void testProcessRequest() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.PING);
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.Data.Ping.DETAILS, dbutil.crypto.encrypt(createDetailsEntry(1L).toString()));
		out.put(Packet.DATA, dataOut.toString());

		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Assert.assertTrue(Constants.equal(Data.Ping.Response.SUCCESS,
				Constants.getEnum(Data.Ping.Response.values(), inData.get(Data.Ping.RESPONSE))));
	}

	@Test
	public void testProcessRequestNotOnline() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.PING);
		MapData dataOut = dbutil.pd.getMapData();
		dataOut.put(Packet.Data.Ping.DETAILS, dbutil.crypto.encrypt(createDetailsEntry(0L).toString()));
		out.put(Packet.DATA, dataOut.toString());

		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Assert.assertTrue(Constants.equal(Data.Ping.Response.NOT_ONLINE,
				Constants.getEnum(Data.Ping.Response.values(), inData.get(Data.Ping.RESPONSE))));
	}

}
