package se.nordicehealth.servlet.implementation.requestprocessing.user;

import static se.nordicehealth.common.implementation.Packet.DATA;
import static se.nordicehealth.common.implementation.Packet.TYPE;

import se.nordicehealth.common.implementation.Packet.Data;
import se.nordicehealth.common.implementation.Packet.Types;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.implementation.Crypto;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class ValidatePatientID extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private Crypto crypto;
	private se.nordicehealth.servlet.core.PPCLocale locale;

	public ValidatePatientID(PPCUserManager um, PPCDatabase db, IPacketData packetData, PPCLogger logger, Crypto crypto, se.nordicehealth.servlet.core.PPCLocale locale) {
		super(packetData, logger, um);
		this.db = db;
		this.crypto = crypto;
		this.locale = locale;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.VALIDATE_PID);

		MapData data = packetData.getMapData();
		Data.ValidatePatientID.Response result = Data.ValidatePatientID.Response.FAIL;
		try {
			if (validatePersonalID(packetData.getMapData(in.get(DATA)))) {
				result = Data.ValidatePatientID.Response.SUCCESS;
			}
		} catch (Exception ignored) { }
		data.put(Data.ValidatePatientID.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private boolean validatePersonalID(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.ValidatePatientID.DETAIL)));
		MapData patient = packetData.getMapData(crypto.decrypt(in.get(Data.ValidatePatientID.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.ValidatePatientID.Details.UID));
		refreshTimer(uid);
		String personalID = patient.get(Data.ValidatePatientID.Patient.PERSONAL_ID);
		
		return db.getUser(um.nameForUID(uid)) != null
				&& locale.formatPersonalID(personalID) != null;
	}
}