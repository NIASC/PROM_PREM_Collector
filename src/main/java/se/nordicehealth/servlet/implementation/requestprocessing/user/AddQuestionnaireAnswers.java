package se.nordicehealth.servlet.implementation.requestprocessing.user;

import static se.nordicehealth.common.implementation.Packet.DATA;
import static se.nordicehealth.common.implementation.Packet.TYPE;

import java.util.ArrayList;
import java.util.List;

import se.nordicehealth.common.implementation.Packet.Data;
import se.nordicehealth.common.implementation.Packet.Types;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.ListData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.LoggedInRequestProcesser;
import se.nordicehealth.servlet.implementation.requestprocessing.QDBFormat;

public class AddQuestionnaireAnswers extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private QDBFormat qdbf;
	private PPCStringScramble encryption;
	private se.nordicehealth.servlet.core.PPCLocale locale;
	private PPCEncryption crypto;

	public AddQuestionnaireAnswers(IPacketData packetData, PPCLogger logger, PPCUserManager um, PPCDatabase db, QDBFormat qdbf,
			PPCStringScramble encryption, se.nordicehealth.servlet.core.PPCLocale locale, PPCEncryption crypto) {
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
		if (forename == null || surname == null) {
			throw new NullPointerException("malformed patient name");
		}
		if (personalID == null) {
			throw new NullPointerException("malformed patient personal id");
		}
		String identifier = encryption.hashMessage(
				forename, personalID, surname);

		List<String> answers = new ArrayList<String>();
		ListData m = packetData.getListData(in.get(Data.AddQuestionnaireAnswers.QUESTIONS));
		for (String str : m.iterable()) {
			answers.add(qdbf.getDBFormat(packetData.getMapData(str)));
		}
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
}