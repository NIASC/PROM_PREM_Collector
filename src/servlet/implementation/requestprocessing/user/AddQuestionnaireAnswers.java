package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import java.util.ArrayList;
import java.util.List;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Implementations;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class AddQuestionnaireAnswers extends LoggedInRequestProcesser {

	public AddQuestionnaireAnswers(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, ServletLogger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.ADD_QANS);

		MapData data = packetData.getMapData();
		Data.AddQuestionnaireAnswers.Response result = Data.AddQuestionnaireAnswers.Response.FAIL;
		try {
			if (storeQestionnaireAnswers(packetData.getMapData(in.get(DATA)))) { result = Data.AddQuestionnaireAnswers.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(Data.AddQuestionnaireAnswers.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private boolean storeQestionnaireAnswers(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.DETAILS)));
		MapData patient = packetData.getMapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.AddQuestionnaireAnswers.Details.UID));
		refreshTimer(uid);
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		servlet.core.interfaces.Locale loc = Implementations.Locale();
		String forename = patient.get(Data.AddQuestionnaireAnswers.Patient.FORENAME);
		String personalID = loc.formatPersonalID(patient.get(Data.AddQuestionnaireAnswers.Patient.PERSONAL_ID));
		String surname = patient.get(Data.AddQuestionnaireAnswers.Patient.SURNAME);
		if (personalID == null)
			throw new NullPointerException("malformed patient personal id");
		String identifier = Implementations.Encryption().hashMessage(
				forename, personalID, surname);

		List<String> answers = new ArrayList<String>();
		ListData m = packetData.getListData(in.get(Data.AddQuestionnaireAnswers.QUESTIONS));
		for (String str : m.iterable())
			answers.add(qdbf.getDBFormat(packetData.getMapData(str)));
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
}