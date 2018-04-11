package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Implementations;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class ValidatePatientID extends LoggedInRequestProcesser {

	public ValidatePatientID(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.VALIDATE_PID);

		MapData data = packetData.getMapData();
		Data.ValidatePatientID.Response result = Data.ValidatePatientID.Response.FAIL;
		try {
			if (validatePersonalID(packetData.getMapData(in.get(DATA)))) { result = Data.ValidatePatientID.Response.SUCCESS; }
		} catch (Exception ignored) { }
		data.put(Data.ValidatePatientID.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private boolean validatePersonalID(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.ValidatePatientID.DETAIL)));
		MapData patient = packetData.getMapData(Crypto.decrypt(in.get(Data.ValidatePatientID.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.ValidatePatientID.Details.UID));
		refreshTimer(uid);
		String personalID = patient.get(Data.ValidatePatientID.Patient.PERSONAL_ID);
		servlet.core.interfaces.Locale loc = Implementations.Locale();
		
		return db.getUser(um.nameForUID(uid)) != null
				&& loc.formatPersonalID(personalID) != null;
	}
}