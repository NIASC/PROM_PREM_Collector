package servlet.implementation.requestprocessing;

import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io.PacketData;

public abstract class IdleRequestProcesser extends RequestProcesser {

	public IdleRequestProcesser(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, ServletLogger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public boolean refreshTimer(long uid) {
		return um.refreshInactivityTimer(uid);
	}
}
