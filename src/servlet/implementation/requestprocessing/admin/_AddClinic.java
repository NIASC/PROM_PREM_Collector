package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class _AddClinic extends RequestProcesser {
	
	public _AddClinic(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.ADD_CLINIC);

		MapData data = packetData.getMapData();
		AdminData.AdminAddClinic.Response result = AdminData.AdminAddClinic.Response.FAIL;
		try {
			if (_storeClinic(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminAddClinic.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminAddClinic.RESPONSE, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private boolean _storeClinic(MapData in) throws Exception {
		String name = in.get(AdminData.AdminAddClinic.NAME);
		return db.addClinic(name);
	}
}