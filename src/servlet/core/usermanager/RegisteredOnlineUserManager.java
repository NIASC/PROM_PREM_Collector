package servlet.core.usermanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegisteredOnlineUserManager {
	public RegisteredOnlineUserManager() {
		users = new HashMap<String, UserData>();
		uids = new HashMap<Long, String>();
	}
	
	public synchronized void registerOnlineUser(UserData user) {
		if (user == null || user.Name() == null || user.UID() == 0L ||
				users.containsKey(user.Name()) || uids.containsKey(user.UID())) {
			return;
		}
		users.put(user.Name(), user);
		uids.put(user.UID(), user.Name());
	}
	
	public synchronized void deregisterOnlineUser(UserData user) {
		users.remove(uids.remove(user.UID()));
	}
	
	public synchronized void deregisterOnlineUser(long uid) {
		users.remove(uids.remove(uid));
	}

	public synchronized void deregisterAllOnlineUsers() {
		users.clear();
		uids.clear();
	}

	public Collection<UserData> iterable() { return users.values(); }
	public int registeredUsersOnline() { return users.size(); }
	public boolean isOnline(String name) { return users.containsKey(name); }
	public boolean isOnline(long uid) { return uids.containsKey(uid); }
	public UserData getUser(String name) { return users.get(name); }
	public UserData getUser(long uid) { return getUser(uids.get(uid)); }
	
	public synchronized String nameForUID(long uid) {
		for (UserData user : iterable()) {
			if (user.UID() == uid) {
				return user.Name();
			}
		}
		return null;
	}
	
	public synchronized boolean refreshInactivityTimer(long uid) {
		UserData user = getUser(uid);
		if (user == null) {
			return false;
		}
		user.refreshAll();
		return true;
	}
	
	public synchronized boolean refreshIdleTimer(long uid) {
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