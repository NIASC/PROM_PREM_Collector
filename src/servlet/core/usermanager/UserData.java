package servlet.core.usermanager;

class UserData {
	private String name;
	private int idleCycles = 0, inactiveCycles = 0;
	private long uid;
	
	UserData(String name, long uid) { this.name = name; this.uid = uid; }
	long UID() { return uid; }
	String Name() { return name; }
	void tickAll() { tickIdle(); tickInactive(); }
	void refreshAll() { refreshIdle(); refreshInactive(); }
	void refreshIdle() { idleCycles = 0; }
	void refreshInactive() { inactiveCycles = 0; }
	void tickIdle() { idleCycles++; }
	void tickInactive() { inactiveCycles++; }
	boolean idleGreaterThan(int number) { return idleCycles > number; }
	boolean inactiveGreaterThan(int number) { return inactiveCycles > number; }
}