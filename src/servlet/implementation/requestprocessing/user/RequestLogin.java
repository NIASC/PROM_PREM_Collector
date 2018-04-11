package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Constants;
import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.SHAEncryption;
import servlet.implementation.User;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class RequestLogin extends RequestProcesser {
	
	public RequestLogin(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	Encryption crypto = SHAEncryption.instance;
	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.REQ_LOGIN);

		MapData data = packetData.getMapData();
		Data.RequestLogin.Response response = Data.RequestLogin.Response.INVALID_DETAILS;
		Data.RequestLogin.UpdatePassword update_password = Data.RequestLogin.UpdatePassword.NO;
		String uid = Long.toString(0L);
		try {
			UserLogin ret = login(packetData.getMapData(in.get(DATA)));
			response = ret.response;
			if (ret.user.update_password) {
				update_password = Data.RequestLogin.UpdatePassword.YES;
			}
			if (Constants.equal(ret.response, Data.RequestLogin.Response.SUCCESS)) {
				uid = Long.toString(ret.uid);
			}
		} catch (Exception e) { }
		data.put(Data.RequestLogin.RESPONSE, response);
		data.put(Data.RequestLogin.UPDATE_PASSWORD, update_password);
		data.put(Data.RequestLogin.UID, uid);

		out.put(DATA, data.toString());
		return out;
	}
	
	private UserLogin login(MapData in) throws Exception {
		UserLogin ret = new UserLogin();
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestLogin.DETAILS)));
		ret.user = db.getUser(inpl.get(Data.RequestLogin.Details.USERNAME));
		if (!ret.user.passwordMatches(inpl.get(Data.RequestLogin.Details.PASSWORD))) {
			throw new NullPointerException("invalid details");
		}

		ret.uid = 0L;
		final int MAX_ATTEMPTS = 10;
		for (int i = 0; ret.uid == 0L && i < MAX_ATTEMPTS; ++i) {
			String hash = crypto.hashMessage(
					Long.toHexString(System.currentTimeMillis()),
					ret.user.name, crypto.generateNewSalt());
			long uid = Long.parseUnsignedLong(hash.substring(0, 2*Long.BYTES), 16);
			if (um.isAvailable(uid)) {
				ret.uid = uid;
				ret.response = um.addUserToListOfOnline(ret.user.name, ret.uid);
			}
		}
		if (ret.uid == 0L) {
			ret.response = Data.RequestLogin.Response.FAIL;
		}
		
		return ret;
	}
	
	private class UserLogin {
		User user;
		long uid;
		Data.RequestLogin.Response response;
	}
}