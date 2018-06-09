package se.nordicehealth.zzphony;

import se.nordicehealth.common.implementation.Packet.Data.RequestLogin.Response;
import se.nordicehealth.servlet.core.PPCUserManager;

public class PhonyUserManager implements PPCUserManager {

	@Override
	public boolean refreshInactivityTimer(long uid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOnline(long uid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String nameForUID(long uid) {
		// TODO Auto-generated method stub
		return "phony";
	}

	@Override
	public boolean isAvailable(long uid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Response addUserToListOfOnline(String username, long uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean delUserFromListOfOnline(long uid) {
		// TODO Auto-generated method stub
		return false;
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
		return false;
	}

}
