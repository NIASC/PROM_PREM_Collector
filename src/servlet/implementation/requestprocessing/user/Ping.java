package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.PPCLogger;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.IdleRequestProcesser;

public class Ping extends IdleRequestProcesser {
	private Crypto crypto;
	
	public Ping(IPacketData packetData, PPCLogger logger, UserManager um, Crypto crypto) {
		super(packetData, logger, um);
		this.crypto = crypto;
	}

	@Override
	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.PING);

		MapData data = packetData.getMapData();
		Data.Ping.Response result = Data.Ping.Response.FAIL;
		try {
			result = processPing(packetData.getMapData(in.get(DATA)));
		} catch (Exception ignored) { }
		data.put(Data.Ping.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private Data.Ping.Response processPing(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.Ping.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.Ping.Details.UID));
		if (um.isOnline(uid)) {
			return refreshTimer(uid) ? Data.Ping.Response.SUCCESS : Data.Ping.Response.FAIL;
		}
		return Data.Ping.Response.NOT_ONLINE;
	}
}