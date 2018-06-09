package se.nordicehealth.servlet.implementation.requestprocessing.admin;

import static se.nordicehealth.servlet.implementation.AdminPacket._DATA;
import static se.nordicehealth.servlet.implementation.AdminPacket._TYPE;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.implementation.User;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminData;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.RequestProcesser;

public class _GetUser extends RequestProcesser {
	private PPCDatabase db;
	
	public _GetUser(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.GET_USER);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = _retrieveUser(packetData.getMapData(in.get(_DATA))).toString();
		} catch (Exception e) { }
		data.put(AdminData.AdminGetUser.USER, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _retrieveUser(MapData in) throws Exception {
		User _user = db.getUser(in.get(AdminData.AdminGetUser.USERNAME));
		MapData user = packetData.getMapData();
		user.put(AdminData.AdminGetUser.User.CLINIC_ID, Integer.toString(_user.clinic_id));
		user.put(AdminData.AdminGetUser.User.USERNAME, _user.name);
		user.put(AdminData.AdminGetUser.User.PASSWORD, _user.password);
		user.put(AdminData.AdminGetUser.User.EMAIL, _user.email);
		user.put(AdminData.AdminGetUser.User.SALT, _user.salt);
		user.put(AdminData.AdminGetUser.User.UPDATE_PASSWORD,
				_user.update_password ? AdminData.AdminGetUser.User.UpdatePassword.YES : AdminData.AdminGetUser.User.UpdatePassword.YES);
		return user;
	}
}