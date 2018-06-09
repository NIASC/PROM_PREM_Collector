package se.nordicehealth.servlet.core.usermanager;

public class ConnectionData {
	private String identifier;
	private int idleCycles = 0, inactiveCycles = 0;
	private long uid;
	
	public ConnectionData(String identifier, long uid) { this.identifier = identifier; this.uid = uid; }
	public long UID() { return uid; }
	public String identifier() { return identifier; }
	public void tickAll() { tickIdle(); tickInactive(); }
	public void refreshAll() { refreshIdle(); refreshInactive(); }
	public void refreshIdle() { idleCycles = 0; }
	public void refreshInactive() { inactiveCycles = 0; }
	public void tickIdle() { idleCycles++; }
	public void tickInactive() { inactiveCycles++; }
	public boolean idleGreaterThan(int number) { return idleCycles > number; }
	public boolean inactiveGreaterThan(int number) { return inactiveCycles > number; }
}