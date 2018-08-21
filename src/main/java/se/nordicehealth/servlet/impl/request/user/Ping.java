package se.nordicehealth.servlet.impl.request.user;

import static se.nordicehealth.common.impl.Packet.DATA;
import static se.nordicehealth.common.impl.Packet.TYPE;

import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.common.impl.Packet.Types;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.IdleRequestProcesser;

public class Ping extends IdleRequestProcesser {
	private PPCEncryption crypto;
	
	public Ping(IPacketData packetData, PPCLogger logger, PPCUserManager um, PPCEncryption crypto) {
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