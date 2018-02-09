package servlet.core;

import java.util.HashMap;
import java.util.Map;

import common.implementation.Packet.Data;

public enum UserManager
{
	MANAGER;
	
	public synchronized Data.RequestLogin.Response addUserToListOfOnline(String username, long uid)
	{
		if (username == null || username.isEmpty()) {
			return Data.RequestLogin.Response.ERROR;
		}
		if (users.size() >= MAX_USERS) {
			return Data.RequestLogin.Response.SERVER_FULL;
		}
		if (users.containsKey(username)) {
			return Data.RequestLogin.Response.ALREADY_ONLINE;
		}
		addUser(username, uid);
		return Data.RequestLogin.Response.SUCCESS;
	}
	
	public synchronized boolean delUserFromListOfOnline(String username) {
		if (username == null || username.isEmpty() || !users.containsKey(username)) {
			return false;
		}
		delUser(username);
		return true;
	}
	
	public boolean refreshIdleTimer(long uid) {
		synchronized(users) {
			UserData user = users.get(nameForUID(uid));
			if (user == null) {
				return false;
			}
			user.loginTimer = 0;
			return true;
		}
	}
	
	public String nameForUID(long uid) {
		synchronized(users) {
			for (UserData user : users.values()) {
				if (user.uid == uid) {
					return user.name;
				}
			}
			return null;
		}
	}
	
	public synchronized void terminate() {
		running = false;
		if (monitor.isAlive()) {
			try { monitor.join(0); } catch (InterruptedException e) { }
		}
		kickAllUsers();
	}
	
	private static final int MAX_USERS = 10;
	private volatile Map<String, UserData> users;
	private volatile boolean running;
	private Thread monitor;

	private UserManager() {
		users = new HashMap<String, UserData>();
		
		running = true;
		monitor = new Thread(new ActivityMonitor());
		monitor.start();
	}
	
	private void addUser(String username, long uid) {
		users.put(username, new UserData(username, uid));
	}
	
	private void delUser(String username) {
		users.remove(username);
	}
	
	private void kickUser(UserData inactive) {
		delUser(inactive.name);
	}
	
	private void kickAllUsers() {
		synchronized(users) {
			for (UserData user : users.values()) {
				kickUser(user);
			}
		}
	}
	
	private class UserData {
		String name;
		int loginTimer;
		long uid;
		
		UserData(String name, long uid) {
			this.name = name;
			loginTimer = 0;
			this.uid = uid;
		}
	}
	
	private class ActivityMonitor implements Runnable {
		final int inactiveTimer = 20; // cycles
		final long cycleTimeMillis = 1000; // ms
		
		@Override
		public void run() {
			while (running) {
				tickActivityTimer();
				sleepFor(cycleTimeMillis);
			}
		}

		private void sleepFor(long millis) {
			try { Thread.sleep(millis); } catch (InterruptedException e) { }
		}

		private void tickActivityTimer() {
			synchronized(users) {
				for (UserData user : users.values()) {
					if (user.loginTimer++ > inactiveTimer) {
						kickUser(user);
					}
				}
			}
		}
	}
}
