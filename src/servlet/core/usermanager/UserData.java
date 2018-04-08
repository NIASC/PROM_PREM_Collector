package servlet.core.usermanager;

public class UserData {
	private String name;
	private int idleCycles = 0, inactiveCycles = 0;
	private long uid;
	
	public UserData(String name, long uid) { this.name = name; this.uid = uid; }
	public long UID() { return uid; }
	public String Name() { return name; }
	public void tickAll() { tickIdle(); tickInactive(); }
	public void refreshAll() { refreshIdle(); refreshInactive(); }
	public void refreshIdle() { idleCycles = 0; }
	public void refreshInactive() { inactiveCycles = 0; }
	public void tickIdle() { idleCycles++; }
	public void tickInactive() { inactiveCycles++; }
	public boolean idleGreaterThan(int number) { return idleCycles > number; }
	public boolean inactiveGreaterThan(int number) { return inactiveCycles > number; }
}