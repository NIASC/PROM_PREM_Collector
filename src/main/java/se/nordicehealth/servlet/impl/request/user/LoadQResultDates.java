package se.nordicehealth.servlet.impl.request.user;

import static se.nordicehealth.common.impl.Packet.DATA;
import static se.nordicehealth.common.impl.Packet.TYPE;

import java.util.List;

import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.common.impl.Packet.Types;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;

public class LoadQResultDates extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private PPCEncryption crypto;
	
	public LoadQResultDates(IPacketData packetData, PPCLogger logger, PPCUserManager um, PPCDatabase db, PPCEncryption crypto) {
		super(packetData, logger, um);
		this.db = db;
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.LOAD_QR_DATE);

		MapData data = packetData.getMapData();
		String result = packetData.getListData().toString();
		try {
			result = retrieveQResultDates(packetData.getMapData(in.get(DATA))).toString();
		} catch (Exception e) {
			logger.log("Error retrieveing questionnaire result dates", e);
		}
		data.put(Data.LoadQResultDates.DATES, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private ListData retrieveQResultDates(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.LoadQResultDates.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.LoadQResultDates.Details.UID));
		refreshTimer(uid);
		User user = db.getUser(um.nameForUID(uid));
		if (user == null) {
			return packetData.getListData();
		}
		List<String> dlist = db.loadQuestionResultDates(user.clinic_id);

		ListData dates = packetData.getListData();
		for (String str : dlist) {
			dates.add(str);
		}
		return dates;
	}
}