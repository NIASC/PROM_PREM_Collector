package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import java.util.List;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.User;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class LoadQResultDates extends LoggedInRequestProcesser {
	
	public LoadQResultDates(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
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
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.LoadQResultDates.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.LoadQResultDates.Details.UID));
		refreshTimer(uid);
		User user = db.getUser(um.nameForUID(uid));
		List<String> dlist = db.loadQuestionResultDates(user.clinic_id);

		ListData dates = packetData.getListData();
		for (String str : dlist) {
			dates.add(str);
		}
		return dates;
	}
}