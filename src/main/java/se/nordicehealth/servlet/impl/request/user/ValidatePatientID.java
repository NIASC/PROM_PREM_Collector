package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;

public class ValidatePatientID extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private PPCEncryption crypto;
	private se.nordicehealth.servlet.core.PPCLocale locale;

	public ValidatePatientID(PPCUserManager um, PPCDatabase db, IPacketData packetData, PPCLogger logger, PPCEncryption crypto, se.nordicehealth.servlet.core.PPCLocale locale) {
		super(packetData, logger, um);
		this.db = db;
		this.crypto = crypto;
		this.locale = locale;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.VALIDATE_PID);

		MapData data = packetData.getMapData();
		String result = Packet.FAIL;
		try {
			if (validatePersonalID(packetData.getMapData(in.get(Packet.DATA)))) {
				result = Packet.SUCCESS;
			}
		} catch (Exception ignored) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private boolean validatePersonalID(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAIL)));
		MapData patient = packetData.getMapData(crypto.decrypt(in.get(Packet.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Packet.UID));
		refreshTimer(uid);
		String personalID = patient.get(Packet.PERSONAL_ID);
		
		return db.getUser(um.nameForUID(uid)) != null
				&& locale.formatPersonalID(personalID) != null;
	}
}