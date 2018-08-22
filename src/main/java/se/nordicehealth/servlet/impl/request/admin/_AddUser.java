package se.nordicehealth.servlet.impl.request.admin;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class _AddUser extends RequestProcesser {
	private PPCDatabase db;
	
	public _AddUser(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._ADD_USER);

		MapData data = packetData.getMapData();
		String result = AdminPacket.FAIL;
		try {
			if (_storeUser(packetData.getMapData(in.get(AdminPacket._DATA)))) {
				result = AdminPacket.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(AdminPacket.RESPONSE, result);

		out.put(AdminPacket._DATA, data.toString());
		return out;
	}
	
	private boolean _storeUser(MapData in) throws Exception {
		MapData details = packetData.getMapData(in.get(AdminPacket.DETAILS));
		
		int clinic_id = Integer.parseInt(details.get(AdminPacket.CLINIC_ID));
		if (!db.getClinics().containsKey(clinic_id)) {
			return false;
		}
		String name = details.get(AdminPacket.NAME);
		if (!validString(name)) {
			return false;
		}
		String password = details.get(AdminPacket.PASSWORD);
		if (!validString(password)) {
			return false;
		}
		String email = details.get(AdminPacket.EMAIL);
		if (!validString(email) || !email.contains("@")) {
			return false;
		}
		String salt = details.get(AdminPacket.SALT);
		if (!validString(salt)) {
			return false;
		}
		
		return db.addUser(clinic_id, name, password, email, salt);
	}
	
	private boolean validString(String str) {
		return str != null && !str.trim().isEmpty();
	}
}