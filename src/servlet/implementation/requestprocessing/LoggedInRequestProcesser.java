package servlet.implementation.requestprocessing;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io._PacketData;

public abstract class LoggedInRequestProcesser extends RequestProcesser {

	public LoggedInRequestProcesser(UserManager um, Database db, _PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public boolean refreshTimer(long uid) {
		return um.refreshInactivityTimer(uid);
	}
}
