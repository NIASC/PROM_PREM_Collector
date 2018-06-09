package se.nordicehealth.servlet.core;

import se.nordicehealth.common.implementation.Packet.Data.RequestLogin.Response;

public interface PPCUserManager {
	boolean refreshInactivityTimer(long uid);
	boolean refreshIdleTimer(long uid);
	boolean isOnline(long uid);
	String nameForUID(long uid);
	boolean isAvailable(long uid);
	Response addUserToListOfOnline(String username, long uid);
	boolean delUserFromListOfOnline(long uid);
	void startManagement();
	void stopManagement();
}
