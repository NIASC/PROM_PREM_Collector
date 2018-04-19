package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import servlet.core.PPCDatabase;
import servlet.core.PPCLogger;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

public class _AddUser extends RequestProcesser {
	private PPCDatabase db;
	
	public _AddUser(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.ADD_USER);

		MapData data = packetData.getMapData();
		AdminData.AdminAddUser.Response result = AdminData.AdminAddUser.Response.FAIL;
		try {
			if (_storeUser(packetData.getMapData(in.get(_DATA)))) {
				result = AdminData.AdminAddUser.Response.SUCCESS;
			}
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