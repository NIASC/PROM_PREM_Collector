package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.statistics.StatisticsContainer;
import servlet.core.statistics.StatisticsData;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.User;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.LoggedInRequestProcesser;

public class LoadQResults extends LoggedInRequestProcesser {
	
	public LoadQResults(UserManager um, Database db, _PacketData packetData, QDBFormat qdbf, _Logger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.LOAD_QR);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = retrieveQResults(packetData.getMapData(in.get(DATA))).toString();
		} catch (Exception e) {
			logger.log("Error retrieveing questionnaire results", e);
		}
		data.put(Data.LoadQResults.RESULTS, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData retrieveQResults(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.LoadQResults.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.LoadQResults.Details.UID));
		refreshTimer(uid);
		User _user = db.getUser(um.nameForUID(uid));
		ListData questions = packetData.getListData(in.get(Data.LoadQResults.QUESTIONS));
		List<Integer> qlist = new ArrayList<Integer>();
		for (String str : questions.iterable()) {
			qlist.add(Integer.parseInt(str));
		}
		
		List<Map<Integer, String>> _results = db.loadQuestionResults(
				_user.clinic_id, qlist,
				getDate(in.get(Data.LoadQResults.BEGIN)),
				getDate(in.get(Data.LoadQResults.END)));
		
		if (_results.size() < 5) {
			logger.log(String.format("Attempted to load %d endries which is less than 5 entries from database", _results.size()));
			return packetData.getMapData();
		} else {
			StatisticsContainer container = new StatisticsContainer();
			for (Map<Integer, String> ansmap : _results) {
				for (Entry<Integer, String> e : ansmap.entrySet()) {
					container.addResult(qdbf.getQFormat(e.getKey(), e.getValue()));
				}
			}

			MapData results = packetData.getMapData();
			for (StatisticsData sd : container.getStatistics()) {
				MapData identifiersAndCount = packetData.getMapData();
				for (Entry<Object, Integer> e : sd.getIdentifiersAndCount()) {
					identifiersAndCount.put(e.getKey().toString(), e.getValue());
				}
				results.put(sd.getQuestionID(), identifiersAndCount.toString());
			}

			return results;
		}
	}

	private static Date getDate(String date) {
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0L);
		}
	}
}