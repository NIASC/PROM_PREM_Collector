package se.nordicehealth.servlet.implementation.requestprocessing.admin;

import static se.nordicehealth.servlet.implementation.AdminPacket._DATA;
import static se.nordicehealth.servlet.implementation.AdminPacket._TYPE;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminData;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.RequestProcesser;

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
		if (!db.getClinics().containsKey(clinic_id)) {
			return false;
		}
		String name = details.get(AdminData.AdminAddUser.Details.NAME);
		if (!validString(name)) {
			return false;
		}
		String password = details.get(AdminData.AdminAddUser.Details.PASSWORD);
		if (!validString(password)) {
			return false;
		}
		String email = details.get(AdminData.AdminAddUser.Details.EMAIL);
		if (!validString(email) || !email.contains("@")) {
			return false;
		}
		String salt = details.get(AdminData.AdminAddUser.Details.SALT);
		if (!validString(salt)) {
			return false;
		}
		
		return db.addUser(clinic_id, name, password, email, salt);
	}
	
	private boolean validString(String str) {
		return str != null && !str.trim().isEmpty();
	}
}