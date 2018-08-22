package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
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
		out.put(Packet.TYPE, Packet.PING);

		MapData data = packetData.getMapData();
		String result = Packet.FAIL;
		try {
			result = processPing(packetData.getMapData(in.get(Packet.DATA)));
		} catch (Exception ignored) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private String processPing(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		long uid = Long.parseLong(inpl.get(Packet.UID));
		if (um.isOnline(uid)) {
			return refreshTimer(uid) ? Packet.SUCCESS : Packet.FAIL;
		}
		return Packet.NOT_ONLINE;
	}
}