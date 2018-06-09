package se.nordicehealth.servlet.core.usermanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RegisteredOnlineUserManager implements ConnectionManager {
	public RegisteredOnlineUserManager() {
		users = new HashMap<String, ConnectionData>();
		uids = new HashMap<Long, String>();
	}
	
	@Override
	public synchronized void registerConnection(ConnectionData user) {
		if (user == null || user.identifier() == null || user.UID() == 0L ||
				users.containsKey(user.identifier()) || uids.containsKey(user.UID())) {
			return;
		}
		users.put(user.identifier(), user);
		uids.put(user.UID(), user.identifier());
	}

	@Override
	public synchronized void deregisterConnection(ConnectionData user) {
		users.remove(uids.remove(user.UID()));
	}

	@Override
	public synchronized void deregisterConnection(long uid) {
		users.remove(uids.remove(uid));
	}

	@Override
	public synchronized void deregisterAllConnections() {
		users.clear();
		uids.clear();
	}

	@Override
	public Collection<ConnectionData> iterable() { return users.values(); }
	@Override
	public int registeredConnections() { return users.size(); }
	@Override
	public boolean isConnected(String name) { return users.containsKey(name); }
	@Override
	public boolean isConnected(long uid) { return uids.containsKey(uid); }
	@Override
	public ConnectionData getConnection(String name) { return users.get(name); }
	@Override
	public ConnectionData getConnection(long uid) { return getConnection(uids.get(uid)); }

	@Override
	public synchronized String identifierForUID(long uid) {
		for (ConnectionData user : iterable()) {
			if (user.UID() == uid) {
				return user.identifier();
			}
		}
		return null;
	}

	@Override
	public synchronized boolean refreshInactivityTimer(long uid) {
		ConnectionData user = getConnection(uid);
		if (user == null) {
			return false;
		}
		user.refreshAll();
		return true;
	}

	@Override
	public synchronized boolean refreshIdleTimer(long uid) {
		ConnectionData user = getConnection(uid);
		if (user == null) {
			return false;
		}
		user.refreshIdle();
		return true;
	}
	
	private Map<String, ConnectionData> users;
	private Map<Long, String> uids;
}