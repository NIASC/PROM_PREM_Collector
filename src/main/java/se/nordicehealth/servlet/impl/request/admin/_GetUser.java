package se.nordicehealth.servlet.impl.request.admin;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class _GetUser extends RequestProcesser {
	private PPCDatabase db;
	
	public _GetUser(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._GET_USER);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = _retrieveUser(packetData.getMapData(in.get(AdminPacket._DATA))).toString();
		} catch (Exception e) { }
		data.put(AdminPacket.USER, result);

		out.put(AdminPacket._DATA, data.toString());
		return out;
	}
	
	private MapData _retrieveUser(MapData in) throws Exception {
		User _user = db.getUser(in.get(AdminPacket.USERNAME));
		MapData user = packetData.getMapData();
		user.put(AdminPacket.CLINIC_ID, Integer.toString(_user.clinic_id));
		user.put(AdminPacket.USERNAME, _user.name);
		user.put(AdminPacket.PASSWORD, _user.password);
		user.put(AdminPacket.EMAIL, _user.email);
		user.put(AdminPacket.SALT, _user.salt);
		user.put(AdminPacket.UPDATE_PASSWORD,
				_user.update_password ? AdminPacket.YES : AdminPacket.YES);
		return user;
	}
}