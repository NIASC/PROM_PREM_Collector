package se.nordicehealth.servlet.impl.request.user;

import java.util.ArrayList;
import java.util.List;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;
import se.nordicehealth.servlet.impl.request.QDBFormat;

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
		out.put(Packet.TYPE, Packet.ADD_QANS);

		MapData data = packetData.getMapData();
		String result = Packet.FAIL;
		try {
			if (storeQestionnaireAnswers(packetData.getMapData(in.get(Packet.DATA)))) {
				result = Packet.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private boolean storeQestionnaireAnswers(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		MapData patient = packetData.getMapData(crypto.decrypt(in.get(Packet.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Packet.UID));
		refreshTimer(uid);
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		String forename = patient.get(Packet.FORENAME);
		String personalID = locale.formatPersonalID(patient.get(Packet.PERSONAL_ID));
		String surname = patient.get(Packet.SURNAME);
		if (forename == null || surname == null) {
			throw new NullPointerException("malformed patient name");
		}
		if (personalID == null) {
			throw new NullPointerException("malformed patient personal id");
		}
		String identifier = encryption.hashMessage(
				forename, personalID, surname);

		List<String> answers = new ArrayList<String>();
		ListData m = packetData.getListData(in.get(Packet.QUESTIONS));
		for (String str : m.iterable()) {
			answers.add(qdbf.getDBFormat(packetData.getMapData(str)));
		}
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
}