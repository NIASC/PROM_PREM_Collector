package servlet.core.usermanager;

class ActivityMonitor implements Runnable
{
	@Override
	public void run() {
		while (running) {
			tickActivityTimer();
			
			sleepFor(millisPerCycle);
		}
	}
	
	void stop() {
		running = false;
		if (monitor.isAlive()) {
			try { monitor.join(0); } catch (InterruptedException e) { }
		}
	}
	
	ActivityMonitor(RegisteredOnlineUserManager monitorTarget) {
		this.usr = monitorTarget;
		running = true;
		(monitor = new Thread(this)).start();
	}

	private Thread monitor;
	private volatile boolean running;
	private final RegisteredOnlineUserManager usr;
	private final int cyclesBeforeIdle = 20;
	private final int cyclesBeforeInactive = 600;
	private final long millisPerCycle = 1000;

	private void tickActivityTimer() {
		synchronized(usr) {
			for (UserData user : usr.iterable()) {
				if (isIdle(user) || isInactive(user)) {
					usr.deregisterOnlineUser(user);
				}
				user.tickAll();
			}
		}
	}

	private void sleepFor(long millis) {
		try { Thread.sleep(millis); } catch (InterruptedException e) { }
	}

	private boolean isInactive(UserData user) {
		return user.inactiveGreaterThan(cyclesBeforeInactive);
	}

	private boolean isIdle(UserData user) {
		return user.idleGreaterThan(cyclesBeforeIdle);
	}
}