package servlet.core.usermanager;

import common.implementation.Packet.Data.RequestLogin.Response;

public class UserManager {

	public UserManager(ConnectionManager usr, ActivityMonitor acmon) {
		this.usr = usr;
		this.acmon = acmon;
	}
	
	public synchronized Response addUserToListOfOnline(String username, long uid) {
		if (username == null || username.isEmpty() || !isAvailable(uid)) {
			return Response.ERROR;
		} else if (usr.registeredConnections() >= MAX_USERS) {
			return Response.SERVER_FULL;
		} else if (usr.isConnected(username)) {
			return Response.ALREADY_ONLINE;
		} else {
			usr.registerConnection(new ConnectionData(username, uid));
			return Response.SUCCESS;
		}
	}
	
	public synchronized boolean delUserFromListOfOnline(long uid) {
		if (usr.isConnected(uid)) {
			usr.deregisterConnection(uid);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean refreshInactivityTimer(long uid) {
		return usr.refreshInactivityTimer(uid);
	}
	
	public boolean refreshIdleTimer(long uid) {
		return usr.refreshIdleTimer(uid);
	}
	
	public String nameForUID(long uid) {
		return usr.identifierForUID(uid);
	}
	
	public boolean isOnline(long uid) {
		return usr.isConnected(uid);
	}
	
	public boolean isAvailable(long uid) {
		return !usr.isConnected(uid);
	}
	
	public void startManagement() {
		acmon.start();
	}
	
	public void stopManagement() {
		acmon.stop();
		usr.deregisterAllConnections();
	}
	
	private static final int MAX_USERS = 10;
	private ConnectionManager usr;
	private ActivityMonitor acmon;
}
