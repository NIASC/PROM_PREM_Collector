package se.nordicehealth.servlet.implementation.requestprocessing.user;

import static se.nordicehealth.common.implementation.Packet.DATA;
import static se.nordicehealth.common.implementation.Packet.TYPE;

import se.nordicehealth.common.implementation.Constants;
import se.nordicehealth.common.implementation.Packet.Data;
import se.nordicehealth.common.implementation.Packet.Types;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.usermanager.UserManager;
import se.nordicehealth.servlet.implementation.Crypto;
import se.nordicehealth.servlet.implementation.User;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.RequestProcesser;

public class RequestLogin extends RequestProcesser {
	private UserManager um;
	private PPCDatabase db;
	private PPCStringScramble encryption;
	private Crypto crypto;
	
	public RequestLogin(IPacketData packetData, PPCLogger logger, UserManager um, PPCDatabase db, PPCStringScramble encryption, Crypto crypto) {
		super(packetData, logger);
		this.um = um;
		this.db = db;
		this.encryption = encryption;
		this.crypto = crypto;
	}

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
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.RequestLogin.DETAILS)));
		ret.user = db.getUser(inpl.get(Data.RequestLogin.Details.USERNAME));
		if (!ret.user.passwordMatches(inpl.get(Data.RequestLogin.Details.PASSWORD))) {
			throw new NullPointerException("invalid details");
		}

		ret.uid = 0L;
		final int MAX_ATTEMPTS = 10;
		for (int i = 0; ret.uid == 0L && i < MAX_ATTEMPTS; ++i) {
			String hash = encryption.hashMessage(
					Long.toHexString(System.currentTimeMillis()),
					ret.user.name, encryption.generateNewSalt());
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