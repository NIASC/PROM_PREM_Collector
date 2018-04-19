package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Constants;
import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.PPCDatabase;
import servlet.core.PPCStringScramble;
import servlet.core.PPCLogger;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.PasswordHandle;
import servlet.implementation.User;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class SetPassword extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private Crypto crypto;
	private PPCStringScramble encryption;
	private PasswordHandle pwdHandle;
	
	public SetPassword(UserManager um, PPCDatabase db, IPacketData packetData, PPCLogger logger, PPCStringScramble hash, Crypto crypto, PasswordHandle pwdHandle) {
		super(packetData, logger, um);
		this.db = db;
		this.encryption = hash;
		this.crypto = crypto;
		this.pwdHandle = pwdHandle;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.SET_PASSWORD);

		MapData data = packetData.getMapData();
		Data.SetPassword.Response result = Data.SetPassword.Response.ERROR;
		try {
			result = storePassword(packetData.getMapData(in.get(DATA)));
		} catch (Exception e) { }
		data.put(Data.SetPassword.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private Data.SetPassword.Response storePassword(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.SetPassword.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.SetPassword.Details.UID));
		refreshTimer(uid);
		String name = um.nameForUID(uid);
		String oldPass = inpl.get(Data.SetPassword.Details.OLD_PASSWORD);
		String newPass1 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD1);
		String newPass2 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD2);

		String newSalt = encryption.generateNewSalt();
		
		User user = db.getUser(name);
		Data.SetPassword.Response status = pwdHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (Constants.equal(status, Data.SetPassword.Response.SUCCESS)) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					encryption.hashMessage(newPass1, newSalt), newSalt);
		}
		return status;
	}
}