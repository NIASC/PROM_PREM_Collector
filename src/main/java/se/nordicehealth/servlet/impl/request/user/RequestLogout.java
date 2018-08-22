package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;

public class RequestLogout extends LoggedInRequestProcesser {
	private PPCEncryption crypto;
	
	public RequestLogout(PPCUserManager um, PPCDatabase db, IPacketData packetData, PPCLogger logger, PPCEncryption crypto) {
		super(packetData, logger, um);
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.REQ_LOGOUT);

		MapData data = packetData.getMapData();
		String result = Packet.ERROR;
		try {
			result = logout(packetData.getMapData(in.get(Packet.DATA)));
		} catch (Exception e) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private String logout(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		long uid = Long.parseLong(inpl.get(Packet.UID));
		refreshTimer(uid);
		return um.delUserFromListOfOnline(uid) ? Packet.SUCCESS : Packet.ERROR;
	}
}