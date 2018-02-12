package servlet.core.usermanager;

import common.implementation.Packet.Data.RequestLogin.Response;

public enum UserManager {
	instance;
	
	public synchronized Response addUserToListOfOnline(String username, long uid)
	{
		if (username == null || username.isEmpty() || !isAvailable(uid)) {
			return Response.ERROR;
		}
		if (usr.registeredUsersOnline() >= MAX_USERS) {
			return Response.SERVER_FULL;
		}
		if (usr.isOnline(username)) {
			return Response.ALREADY_ONLINE;
		}
		usr.registerOnlineUser(new UserData(username, uid));
		return Response.SUCCESS;
	}
	
	public synchronized boolean delUserFromListOfOnline(long uid) {
		if (!usr.isOnline(uid)) {
			return false;
		}
		usr.deregisterOnlineUser(uid);
		return true;
	}
	
	public boolean refreshInactivityTimer(long uid) {
		return usr.refreshInactivityTimer(uid);
	}
	
	public boolean refreshIdleTimer(long uid) {
		return usr.refreshIdleTimer(uid);
	}
	
	public String nameForUID(long uid) {
		return usr.nameForUID(uid);
	}
	
	public boolean isOnline(long uid) {
		return usr.isOnline(uid);
	}
	
	public boolean isAvailable(long uid) {
		return !usr.isOnline(uid);
	}
	
	public void terminate() {
		acmon.stop();
		usr.deregisterAllOnlineUsers();
	}
	
	private static final int MAX_USERS = 10;
	private RegisteredOnlineUserManager usr;
	private ActivityMonitor acmon;

	private UserManager() {
		usr = new RegisteredOnlineUserManager();
		acmon = new ActivityMonitor(usr);
	}
}
