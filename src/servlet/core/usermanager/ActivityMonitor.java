package servlet.core.usermanager;

public class ActivityMonitor implements Runnable
{
	@Override
	public void run() {
		for (; running; sleepFor(millisPerCycle)) {
			tickActivityTimer();
		}
	}
	
	public void stop() {
		running = false;
		if (monitor.isAlive()) {
			try { monitor.join(0); } catch (InterruptedException e) { }
		}
	}
	
	public ActivityMonitor(RegisteredOnlineUserManager monitorTarget) {
		this(monitorTarget, 1000, 20, 600);
	}
	
	public ActivityMonitor(RegisteredOnlineUserManager monitorTarget, long millisPerCycle, int cyclesBeforeIdle, int cyclesBeforeInactive) {
		this.usr = monitorTarget;
		this.cyclesBeforeIdle = cyclesBeforeIdle;
		this.cyclesBeforeInactive = cyclesBeforeInactive;
		this.millisPerCycle = millisPerCycle;
		running = true;
		(monitor = new Thread(this)).start();
	}

	private Thread monitor;
	private volatile boolean running;
	private final RegisteredOnlineUserManager usr;
	private final int cyclesBeforeIdle;
	private final int cyclesBeforeInactive;
	private final long millisPerCycle;

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
		return user.inactiveGreaterThan(cyclesBeforeInactive-1);
	}

	private boolean isIdle(UserData user) {
		return user.idleGreaterThan(cyclesBeforeIdle-1);
	}
}