package servlet.implementation.requestprocessing;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io.PacketData;

public abstract class IdleRequestProcesser extends RequestProcesser {

	public IdleRequestProcesser(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public boolean refreshTimer(long uid) {
		return um.refreshInactivityTimer(uid);
	}
}
