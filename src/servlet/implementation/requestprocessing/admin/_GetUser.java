package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import servlet.core.PPCDatabase;
import servlet.core.PPCLogger;
import servlet.implementation.User;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

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