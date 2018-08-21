package se.nordicehealth.zzphony;

import se.nordicehealth.common.impl.Packet.Data.RequestLogin.Response;
import se.nordicehealth.servlet.core.PPCUserManager;

public class PhonyUserManager implements PPCUserManager {
	
	private boolean uidIsAvailable = true;

	@Override
	public boolean refreshInactivityTimer(long uid) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isOnline(long uid) {
		// TODO Auto-generated method stub
		return uid != 0L;
	}

	@Override
	public String nameForUID(long uid) {
		// TODO Auto-generated method stub
		return "phony";
	}

	@Override
	public boolean isAvailable(long uid) {
		// TODO Auto-generated method stub
		return uidIsAvailable;
	}

	@Override
	public Response addUserToListOfOnline(String username, long uid) {
		// TODO Auto-generated method stub
		return Response.SUCCESS;
	}

	@Override
	public boolean delUserFromListOfOnline(long uid) {
		// TODO Auto-generated method stub
		return isOnline(uid);
	}

	@Override
	public void startManagement() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopManagement() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refreshIdleTimer(long uid) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public void setIsAvailableUID(boolean available) {
		uidIsAvailable = available;
	}

}
