package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class RequestLogin extends RequestProcesser {
	private PPCUserManager um;
	private PPCDatabase db;
	private PPCStringScramble encryption;
	private PPCEncryption crypto;
	
	public RequestLogin(IPacketData packetData, PPCLogger logger, PPCUserManager um, PPCDatabase db, PPCStringScramble encryption, PPCEncryption crypto) {
		super(packetData, logger);
		this.um = um;
		this.db = db;
		this.encryption = encryption;
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.REQ_LOGIN);

		MapData data = packetData.getMapData();
		String response = Packet.INVALID_DETAILS;
		String update_password = Packet.NO;
		String uid = Long.toString(0L);
		try {
			UserLogin ret = login(packetData.getMapData(in.get(Packet.DATA)));
			response = ret.response;
			if (ret.user.update_password) {
				update_password = Packet.YES;
			}
			if (ret.response.equals(Packet.SUCCESS)) {
				uid = Long.toString(ret.uid);
			}
		} catch (Exception e) { }
		data.put(Packet.RESPONSE, response);
		data.put(Packet.UPDATE_PASSWORD, update_password);
		data.put(Packet.UID, uid);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private UserLogin login(MapData in) throws Exception {
		UserLogin ret = new UserLogin();
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		ret.user = db.getUser(inpl.get(Packet.USERNAME));
		if (!ret.user.passwordMatches(inpl.get(Packet.PASSWORD))) {
			throw new NullPointerException("invalid details");
		}

		ret.uid = 0L;
		final int MAX_ATTEMPTS = 10;
		for (int i = 0; ret.uid == 0L && i < MAX_ATTEMPTS; ++i) {
			String hash = encryption.hashMessage(
					Long.toHexString(System.currentTimeMillis()),
					ret.user.name, encryption.generateNewSalt());
			long uid = Long.parseUnsignedLong(hash.substring(0, Math.min(2*Long.BYTES, hash.length())), 16);
			if (um.isAvailable(uid)) {
				ret.uid = uid;
				ret.response = um.addUserToListOfOnline(ret.user.name, ret.uid);
			}
		}
		if (ret.uid == 0L) {
			ret.response = Packet.FAIL;
		}
		
		return ret;
	}
	
	private class UserLogin {
		User user;
		long uid;
		String response;
	}
}