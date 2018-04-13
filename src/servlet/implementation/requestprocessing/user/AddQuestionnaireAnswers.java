package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import java.util.ArrayList;
import java.util.List;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class AddQuestionnaireAnswers extends LoggedInRequestProcesser {
	private Database db;
	private QDBFormat qdbf;
	private Encryption encryption;
	private servlet.core.interfaces._Locale locale;
	private Crypto crypto;

	public AddQuestionnaireAnswers(_PacketData packetData, _Logger logger, UserManager um, Database db, QDBFormat qdbf,
			Encryption encryption, servlet.core.interfaces._Locale locale, Crypto crypto) {
		super(packetData, logger, um);
		this.db = db;
		this.qdbf = qdbf;
		this.encryption = encryption;
		this.locale = locale;
		this.crypto = crypto;
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
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.DETAILS)));
		MapData patient = packetData.getMapData(crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.AddQuestionnaireAnswers.Details.UID));
		refreshTimer(uid);
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		String forename = patient.get(Data.AddQuestionnaireAnswers.Patient.FORENAME);
		String personalID = locale.formatPersonalID(patient.get(Data.AddQuestionnaireAnswers.Patient.PERSONAL_ID));
		String surname = patient.get(Data.AddQuestionnaireAnswers.Patient.SURNAME);
		if (personalID == null) {
			throw new NullPointerException("malformed patient personal id");
		}
		String identifier = encryption.hashMessage(
				forename, personalID, surname);

		List<String> answers = new ArrayList<String>();
		ListData m = packetData.getListData(in.get(Data.AddQuestionnaireAnswers.QUESTIONS));
		for (String str : m.iterable())
			answers.add(qdbf.getDBFormat(packetData.getMapData(str)));
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
}