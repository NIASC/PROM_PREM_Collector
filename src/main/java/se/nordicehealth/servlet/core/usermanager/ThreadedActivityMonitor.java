package se.nordicehealth.servlet.core.usermanager;

public class ThreadedActivityMonitor implements Runnable, ActivityMonitor {
	@Override
	public void run() {
		for (; running; sleepFor(millisPerCycle)) {
			tickActivityTimer();
		}
	}
	
	@Override
	public void start() {
		if (!running) {
			running = true;
			(monitor = new Thread(this)).start();
		}
	}
	
	@Override
	public void stop() {
		if (running) {
			running = false;
			if (monitor.isAlive()) {
				try { monitor.join(0); } catch (InterruptedException e) { }
			}
		}
	}
	
	public ThreadedActivityMonitor(RegisteredOnlineUserManager monitorTarget) {
		this(monitorTarget, 1000, 20, 600);
	}
	
	public ThreadedActivityMonitor(ConnectionManager monitorTarget, long millisPerCycle, int cyclesBeforeIdle, int cyclesBeforeInactive) {
		this.usr = monitorTarget;
		this.cyclesBeforeIdle = cyclesBeforeIdle;
		this.cyclesBeforeInactive = cyclesBeforeInactive;
		this.millisPerCycle = millisPerCycle;
	}

	private Thread monitor;
	private volatile boolean running;
	private final ConnectionManager usr;
	private final int cyclesBeforeIdle;
	private final int cyclesBeforeInactive;
	private final long millisPerCycle;

	private void tickActivityTimer() {
		synchronized(usr) {
			for (ConnectionData user : usr.iterable()) {
				if (isIdle(user) || isInactive(user)) {
					usr.deregisterConnection(user);
				}
				user.tickAll();
			}
		}
	}

	private void sleepFor(long millis) {
		try { Thread.sleep(millis); } catch (InterruptedException e) { }
	}

	private boolean isInactive(ConnectionData user) {
		return user.inactiveGreaterThan(cyclesBeforeInactive-1);
	}

	private boolean isIdle(ConnectionData user) {
		return user.idleGreaterThan(cyclesBeforeIdle-1);
	}
}