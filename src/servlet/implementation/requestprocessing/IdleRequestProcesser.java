package servlet.implementation.requestprocessing;

import servlet.core.PPCLogger;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io.IPacketData;

public abstract class IdleRequestProcesser extends RequestProcesser {
	protected UserManager um;

	public IdleRequestProcesser(IPacketData packetData, PPCLogger logger, UserManager um) {
		super(packetData, logger);
		this.um = um;
	}

	public boolean refreshTimer(long uid) {
		return um.refreshInactivityTimer(uid);
	}
}
