package servlet.implementation.requestprocessing;

import servlet.core._Logger;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io._PacketData;

public abstract class IdleRequestProcesser extends RequestProcesser {
	protected UserManager um;

	public IdleRequestProcesser(_PacketData packetData, _Logger logger, UserManager um) {
		super(packetData, logger);
		this.um = um;
	}

	public boolean refreshTimer(long uid) {
		return um.refreshInactivityTimer(uid);
	}
}
