package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.IdleRequestProcesser;

public class Ping extends IdleRequestProcesser {
	
	public Ping(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
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
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.Ping.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.Ping.Details.UID));
		if (um.isOnline(uid)) {
			return refreshTimer(uid) ? Data.Ping.Response.SUCCESS : Data.Ping.Response.FAIL;
		}
		return Data.Ping.Response.NOT_ONLINE;
	}
}