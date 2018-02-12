package servlet.core.usermanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class RegisteredOnlineUserManager {
	RegisteredOnlineUserManager() {
		users = new HashMap<String, UserData>();
		uids = new HashMap<Long, String>();
	}
	
	synchronized void registerOnlineUser(UserData user) {
		users.put(user.Name(), user);
		uids.put(user.UID(), user.Name());
	}
	
	synchronized void deregisterOnlineUser(UserData user) {
		users.remove(uids.remove(user.UID()));
	}
	
	synchronized void deregisterOnlineUser(long uid) {
		users.remove(uids.remove(uid));
	}

	synchronized void deregisterAllOnlineUsers() {
		for (UserData user : iterable()) {
			deregisterOnlineUser(user);
		}
	}

	Collection<UserData> iterable() { return users.values(); }
	int registeredUsersOnline() { return users.size(); }
	boolean isOnline(String name) { return users.containsKey(name); }
	boolean isOnline(long uid) { return uids.containsKey(uid); }
	UserData getUser(String name) { return users.get(name); }
	UserData getUser(long uid) { return getUser(uids.get(uid)); }
	
	synchronized String nameForUID(long uid) {
		for (UserData user : iterable()) {
			if (user.UID() == uid) {
				return user.Name();
			}
		}
		return null;
	}
	
	synchronized boolean refreshInactivityTimer(long uid) {
		UserData user = getUser(uid);
		if (user == null) {
			return false;
		}
		user.refreshAll();
		return true;
	}
	
	synchronized boolean refreshIdleTimer(long uid) {
		UserData user = getUser(uid);
		if (user == null) {
			return false;
		}
		user.refreshIdle();
		return true;
	}
	
	private Map<String, UserData> users;
	private Map<Long, String> uids;
}