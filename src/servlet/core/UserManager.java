/** UserManager.java
 * 
 * Copyright 2017 Marcus Malmquist
 * 
 * This file is part of PROM_PREM_Collector.
 * 
 * PROM_PREM_Collector is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * PROM_PREM_Collector is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PROM_PREM_Collector.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package servlet.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.implementation.Constants;

/**
 * This class keeps track of which users are online and how many users
 * that can be online at any time. This class adds users to the list
 * of online users if they are allowed to log in, and removes them
 * from the list when they log out.
 * 
 * @author Marcus Malmquist
 *
 */
public class UserManager
{
	/* Public */

	@Override
	public final Object clone()
			throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
	
	/**
	 * 
	 * @return The active instance of this class.
	 */
	public static synchronized UserManager getUserManager()
	{
		if (manager == null)
			manager = new UserManager();
		return manager;
	}
	
	/* Protected */
	
	/**
	 * Adds {@code username} to the list of online users if they exist.
	 * 
	 * @param username The username of the user to add.
	 * 
	 * @return
	 * 		<code>Constants.ERROR_STR</code>
	 * 			If an error occurred.<br>
	 * 		<code>Constants.SERVER_FULL_STR</code>
	 * 			If the server is full.<br>
	 * 		<code>Constants.ALREADY_ONLINE_STR</code>
	 * 			If the user with username {@code username} is already
	 * 			online.<br>
	 * 		<code>Constants.SUCCESS_STR</code>
	 * 			If the user was successfully added.
	 */
	public synchronized int addUser(String username, long uid)
	{
		if (username == null || username.isEmpty())
			return Constants.ERROR;
		if (users.size() >= MAX_USERS)
			return Constants.SERVER_FULL;
		if (users.contains(username))
			return Constants.ALREADY_ONLINE;
		_addUser(username, uid);
		return Constants.SUCCESS;
	}
	
	/**
	 * Removes {@code username} from the list of online users if they exist.
	 * 
	 * @param username The username of the user to delete.
	 * 
	 * @return {@code true} if the user was deleted. False if the user
	 * 		did not exist in the list of users.
	 */
	public synchronized boolean delUser(String username)
	{
		if (username == null || username.isEmpty()
				|| !users.contains(username))
			return false;
		_delUser(username);
		return true;
	}
	
	public String nameForUID(long uid) {
		for (_User user : _users.values())
			if (user.uid == uid)
				return user.name;
		return null;
	}
	
	public void terminate()
	{
		running = false;
		if (monitor.isAlive()) {
			try {
				monitor.join(0);
			} catch (InterruptedException e) {
				
			}
		}
		_kickAllUsers();
	}
	
	/* Private */
	
	private static UserManager manager;
	private static final int MAX_USERS = 10;
	private List<String> users;
	private volatile Map<String, _User> _users;
	private volatile boolean running;
	private Thread monitor;

	/**
	 * Singleton class
	 */
	private UserManager()
	{
		users = new ArrayList<String>();
		_users = new HashMap<String, _User>();
		
		running = true;
		monitor = new Thread(new activityMonitor());
		monitor.start();
	}
	
	private void _addUser(String username, long uid) {
		users.add(username);
		_users.put(username, new _User(username, uid));
	}
	
	private void _delUser(String username) {
		users.remove(username);
		_users.remove(username);
	}
	
	private void _kickUser(_User inactive) {
		_delUser(inactive.name);
	}
	
	private void _kickAllUsers()
	{
		for (_User user : _users.values()) {
			_kickUser(user);
		}
	}
	
	private class _User
	{
		String name;
		int loginTimer;
		long uid;
		
		_User(String name, long uid)
		{
			this.name = name;
			loginTimer = 0;
			this.uid = uid;
		}
	}
	
	private class activityMonitor implements Runnable
	{
		final int inactiveTimer = 360; // cycles
		final long cycleTime = 5000; // ms
		@Override
		public void run() {
			while (running) {
				for (_User user : _users.values())
					if (user.loginTimer++ > inactiveTimer)
						_kickUser(user);
				try {
					Thread.sleep(cycleTime);
				} catch (InterruptedException e) {
					
				}
			}
		}
}
}
