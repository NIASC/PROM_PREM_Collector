package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class _AddUser extends RequestProcesser {
	
	public _AddUser(UserManager um, Database db, _PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.ADD_USER);

		MapData data = packetData.getMapData();
		AdminData.AdminAddUser.Response result = AdminData.AdminAddUser.Response.FAIL;
		try {
			if (_storeUser(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminAddUser.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminAddUser.RESPONSE, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private boolean _storeUser(MapData in) throws Exception {
		MapData details = packetData.getMapData(in.get(AdminData.AdminAddUser.DETAILS));
		int clinic_id = Integer.parseInt(details.get(AdminData.AdminAddUser.Details.CLINIC_ID));
		String name = details.get(AdminData.AdminAddUser.Details.NAME);
		String password = details.get(AdminData.AdminAddUser.Details.PASSWORD);
		String email = details.get(AdminData.AdminAddUser.Details.EMAIL);
		String salt = details.get(AdminData.AdminAddUser.Details.SALT);
		return db.addUser(clinic_id, name, password, email, salt);
	}
}