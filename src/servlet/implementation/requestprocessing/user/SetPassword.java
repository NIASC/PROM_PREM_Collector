package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Constants;
import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.PasswordHandle;
import servlet.implementation.User;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class SetPassword extends LoggedInRequestProcesser {
	
	public SetPassword(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, ServletLogger logger) {
		super(um, db, packetData, qdbf, logger);
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
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.SetPassword.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.SetPassword.Details.UID));
		refreshTimer(uid);
		String name = um.nameForUID(uid);
		String oldPass = inpl.get(Data.SetPassword.Details.OLD_PASSWORD);
		String newPass1 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD1);
		String newPass2 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD2);

		Encryption hash = Implementations.Encryption();
		String newSalt = hash.generateNewSalt();
		
		User user = db.getUser(name);
		Data.SetPassword.Response status = PasswordHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (Constants.equal(status, Data.SetPassword.Response.SUCCESS)) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					hash.hashMessage(newPass1, newSalt), newSalt);
		}
		return status;
	}
}