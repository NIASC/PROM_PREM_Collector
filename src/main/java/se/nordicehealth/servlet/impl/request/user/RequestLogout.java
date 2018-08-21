package se.nordicehealth.servlet.impl.request.user;

import static se.nordicehealth.common.impl.Packet.DATA;
import static se.nordicehealth.common.impl.Packet.TYPE;

import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.common.impl.Packet.Types;
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
		out.put(TYPE, Types.REQ_LOGOUT);

		MapData data = packetData.getMapData();
		Data.RequestLogout.Response result = Data.RequestLogout.Response.ERROR;
		try {
			result = logout(packetData.getMapData(in.get(DATA)));
		} catch (Exception e) { }
		data.put(Data.RequestLogout.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private Data.RequestLogout.Response logout(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.RequestLogout.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.RequestLogout.Details.UID));
		refreshTimer(uid);
		return um.delUserFromListOfOnline(uid) ? Data.RequestLogout.Response.SUCCESS : Data.RequestLogout.Response.ERROR;
	}
}