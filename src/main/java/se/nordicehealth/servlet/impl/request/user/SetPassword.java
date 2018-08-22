package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCPasswordValidation;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;

public class SetPassword extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private PPCEncryption crypto;
	private PPCStringScramble encryption;
	private PPCPasswordValidation pwdHandle;
	
	public SetPassword(PPCUserManager um, PPCDatabase db, IPacketData packetData, PPCLogger logger, PPCStringScramble hash, PPCEncryption crypto, PPCPasswordValidation pwdHandle) {
		super(packetData, logger, um);
		this.db = db;
		this.encryption = hash;
		this.crypto = crypto;
		this.pwdHandle = pwdHandle;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.SET_PASSWORD);

		MapData data = packetData.getMapData();
		String result = Packet.ERROR;
		try {
			result = storePassword(packetData.getMapData(in.get(Packet.DATA)));
		} catch (Exception e) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private String storePassword(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		long uid = Long.parseLong(inpl.get(Packet.UID));
		refreshTimer(uid);
		String name = um.nameForUID(uid);
		String oldPass = inpl.get(Packet.OLD_PASSWORD);
		String newPass1 = inpl.get(Packet.NEW_PASSWORD1);
		String newPass2 = inpl.get(Packet.NEW_PASSWORD2);

		String newSalt = encryption.generateNewSalt();
		
		User user = db.getUser(name);
		String status = pwdHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (status.equals(Packet.SUCCESS)) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					encryption.hashMessage(newPass1, newSalt), newSalt);
		}
		return status;
	}
}