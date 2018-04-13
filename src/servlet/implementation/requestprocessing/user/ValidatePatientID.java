package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class ValidatePatientID extends LoggedInRequestProcesser {
	private Database db;
	private Crypto crypto;
	private servlet.core.interfaces._Locale locale;

	public ValidatePatientID(UserManager um, Database db, _PacketData packetData, _Logger logger, Crypto crypto, servlet.core.interfaces._Locale locale) {
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