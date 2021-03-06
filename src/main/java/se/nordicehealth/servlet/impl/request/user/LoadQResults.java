package se.nordicehealth.servlet.impl.request.user;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.core.stats.StatisticsContainer;
import se.nordicehealth.servlet.core.stats.StatisticsData;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.LoggedInRequestProcesser;
import se.nordicehealth.servlet.impl.request.QDBFormat;

public class LoadQResults extends LoggedInRequestProcesser {
	private PPCDatabase db;
	private QDBFormat qdbf;
	private PPCEncryption crypto;
	
	public LoadQResults(IPacketData packetData, PPCLogger logger, PPCUserManager um, PPCDatabase db, QDBFormat qdbf, PPCEncryption crypto) {
		super(packetData, logger, um);
		this.db = db;
		this.qdbf = qdbf;
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.LOAD_QR);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = retrieveQResults(packetData.getMapData(in.get(Packet.DATA))).toString();
		} catch (Exception e) {
			logger.log("Error retrieveing questionnaire results", e);
		}
		data.put(Packet.RESULTS, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private MapData retrieveQResults(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		long uid = Long.parseLong(inpl.get(Packet.UID));
		refreshTimer(uid);
		User _user = db.getUser(um.nameForUID(uid));
		ListData questions = packetData.getListData(in.get(Packet.QUESTIONS));
		List<Integer> qlist = new ArrayList<Integer>();
		for (String str : questions.iterable()) {
			qlist.add(Integer.parseInt(str));
		}
		
		List<Map<Integer, String>> _results = db.loadQuestionResults(
				_user.clinic_id, qlist,
				getDate(in.get(Packet.BEGIN)),
				getDate(in.get(Packet.END)));
		
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