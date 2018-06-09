package se.nordicehealth.servlet.core.usermanager;

import se.nordicehealth.common.implementation.Packet.Data.RequestLogin.Response;
import se.nordicehealth.servlet.core.PPCUserManager;

public class UserManager implements PPCUserManager {

	/**
	 * @deprecated Use {@link #UserManager(ConnectionManager,ActivityMonitor,int)} instead
	 */
	public UserManager(ConnectionManager usr, ActivityMonitor acmon) {
		this(usr, acmon, 10);
	}

	public UserManager(ConnectionManager usr, ActivityMonitor acmon, int maxConnections) {
		this.usr = usr;
		this.acmon = acmon;
		this.maxConnections = maxConnections;
	}

	@Override
	public synchronized Response addUserToListOfOnline(String username, long uid) {
		if (username == null || username.isEmpty() || !isAvailable(uid)) {
			return Response.ERROR;
		} else if (usr.registeredConnections() >= maxConnections) {
			return Response.SERVER_FULL;
		} else if (usr.isConnected(username)) {
			return Response.ALREADY_ONLINE;
		} else {
			usr.registerConnection(new ConnectionData(username, uid));
			return Response.SUCCESS;
		}
	}

	@Override
	public synchronized boolean delUserFromListOfOnline(long uid) {
		if (usr.isConnected(uid)) {
			usr.deregisterConnection(uid);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean refreshInactivityTimer(long uid) {
		return usr.refreshInactivityTimer(uid);
	}

	@Override
	public boolean refreshIdleTimer(long uid) {
		return usr.refreshIdleTimer(uid);
	}

	@Override
	public String nameForUID(long uid) {
		return usr.identifierForUID(uid);
	}

	@Override
	public boolean isOnline(long uid) {
		return usr.isConnected(uid);
	}

	@Override
	public boolean isAvailable(long uid) {
		return !usr.isConnected(uid);
	}

	@Override
	public void startManagement() {
		acmon.start();
	}
	
	@Override
	public void stopManagement() {
		acmon.stop();
		usr.deregisterAllConnections();
	}
	
	private final int maxConnections;
	private ConnectionManager usr;
	private ActivityMonitor acmon;
}
