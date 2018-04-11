package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class RequestLogout extends LoggedInRequestProcesser {
	
	public RequestLogout(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
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
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestLogout.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.RequestLogout.Details.UID));
		refreshTimer(uid);
		return um.delUserFromListOfOnline(uid) ? Data.RequestLogout.Response.SUCCESS : Data.RequestLogout.Response.ERROR;
	}
}