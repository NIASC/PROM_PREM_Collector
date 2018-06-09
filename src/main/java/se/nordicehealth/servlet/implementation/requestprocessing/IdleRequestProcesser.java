package se.nordicehealth.servlet.implementation.requestprocessing;

import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.usermanager.UserManager;
import se.nordicehealth.servlet.implementation.io.IPacketData;

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
