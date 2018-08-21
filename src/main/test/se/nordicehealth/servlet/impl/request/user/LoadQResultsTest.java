package se.nordicehealth.servlet.impl.request.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.user.LoadQResults;

public class LoadQResultsTest {
	LoadQResults processer;
	ReqProcUtil dbutil;
	Map<Integer, Map<String, Integer>> allResults;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		
		processer = new LoadQResults(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.qdbf, dbutil.crypto);
		
		allResults = calculateStatistics();
	}
	
	private int setNextDatabaseUserCall(String name) {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", name);
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		
		dbutil.rs.setNextInts(ints);
		dbutil.rs.setNextStrings(strings);
		return 1;
	}
	
	private int setNextDatabaseDates(List<Map<String, String>> dates) {
		for (Map<String, String> date : dates) {
			Map<String, String> strings = new HashMap<String, String>();
			strings.putAll(date);
			dbutil.rs.setNextStrings(strings);
			dbutil.rs.setNextInts(new HashMap<String, Integer>());
		}
		return dates.size();
	}
	
	private void setupDatabaseCalls(List<Map<String, String>> dates) {
		int availNextCalls = 0;
		availNextCalls += setNextDatabaseUserCall("phony");
		availNextCalls += setNextDatabaseDates(dates);
		dbutil.rs.setNumberOfAvailableNextCalls(availNextCalls);
	}
	
	private int ids[][] = {
			{1, 2, 0, 0, 0, 1, 1},
			{2, 2, 2, 1, 0, 2, 2},
			{0, 2, 0, 2, 2, 0, 0},
			{1, 2, 2, 1, 1, 2, 0},
			{2, 1, 0, 0, 2, 2, 0},
			{1, 1, 1, 1, 1, 1, 2},
			{2, 2, 0, 1, 1, 2, 0},
			{0, 2, 0, 2, 2, 1, 0},
			{1, 1, 0, 2, 2, 0, 2},
			{1, 1, 0, 2, 0, 1, 0}
	};
	
	private Map<Integer, Map<String, Integer>> calculateStatistics() {
		Map<Integer, Map<String, Integer>> out = new HashMap<Integer, Map<String, Integer>>();
		
		for (int e[] : ids) {
			for (int i = 0; i < e.length; ++i) {
				if (!out.containsKey(i)) { out.put(i, new HashMap<String, Integer>()); }
				
				Map<String, Integer> m = out.get(i);
				String key = Integer.toString(e[i]);
				if (!m.containsKey(key)) { m.put(key, 0); }
				
				m.put(key, m.get(key) + 1);
			}
		}
		return out;
	}
	
	private List<Map<String, String>> createDatabaseQuestionAnswerContent(int nEntries) {
		List<Map<String, String>> out = new ArrayList<Map<String, String>>();
		int iMax = Math.max(ids.length, Math.min(0, nEntries));
		for (int i = 0; i < iMax; ++i) {
			int e[] = ids[i];
			Map<String, String> entry = new TreeMap<String, String>();
			for (int j = 0; j < e.length; ++j) {
				entry.put("question" + j, "option" + e[j]);
			}
			out.add(entry);
		}
		return out;
	}
	
	private Map<Integer, Map<String, Integer>> unpackResult(MapData rlist) {
		Map<Integer, Map<String, Integer>> container = new HashMap<Integer, Map<String, Integer>>();
		for (Entry<String, String> str : rlist.iterable()) {
			MapData ansmap = dbutil.pd.getMapData(str.getValue());
			Map<String, Integer> identifierAndCount = new TreeMap<String, Integer>();
			for (Entry<String, String> e : ansmap.iterable()) {
				identifierAndCount.put(e.getKey(), Integer.parseInt(e.getValue()));
			}
			container.put(Integer.parseInt(str.getKey()), identifierAndCount);
		}
		return container;
	}
	
	private MapData createDataPacket(ListData questionIDs, MapData details) {
		String begin = "1985-01-01";
		String end = "1999-12-31";
		
		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResults.QUESTIONS, questionIDs.toString());
		dataOut.put(Packet.Data.LoadQResults.DETAILS, dbutil.crypto.encrypt(details.toString()));
		dataOut.put(Packet.Data.LoadQResults.BEGIN, begin);
		dataOut.put(Packet.Data.LoadQResults.END, end);
		return dataOut;
	}

	private MapData createDetailsEntry() {
		MapData details = new MapData();
		details.put(Packet.Data.LoadQResults.Details.UID, Long.toString(1L));
		return details;
	}

	private ListData createQuestionsRequestEntry(List<Integer> questionIDs) {
		ListData questions = new ListData();
		for (Integer i : questionIDs) {
			questions.add(Integer.toString(i));
		}
		return questions;
	}
	
	@Test
	public void testProcessRequest() {
		List<Integer> questionIDs = Arrays.asList(1, 2, 4);
		
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR);
		out.put(Packet.DATA, createDataPacket(createQuestionsRequestEntry(questionIDs), createDetailsEntry()).toString());
		
		setupDatabaseCalls(createDatabaseQuestionAnswerContent(ids.length));
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Map<Integer, Map<String, Integer>> container = unpackResult(dbutil.pd.getMapData(inData.get(Packet.Data.LoadQResults.RESULTS)));
		
		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestEmptyList() {
		List<Integer> questionIDs = new ArrayList<Integer>();
		
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR);
		out.put(Packet.DATA, createDataPacket(createQuestionsRequestEntry(questionIDs), createDetailsEntry()).toString());
		
		setupDatabaseCalls(createDatabaseQuestionAnswerContent(ids.length));
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Map<Integer, Map<String, Integer>> container = unpackResult(dbutil.pd.getMapData(inData.get(Packet.Data.LoadQResults.RESULTS)));
		
		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestInvalidUser() {
		List<Integer> questionIDs = Arrays.asList(1, 2, 4);
		
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR);
		out.put(Packet.DATA, createDataPacket(createQuestionsRequestEntry(questionIDs), new MapData()).toString());
		
		setupDatabaseCalls(createDatabaseQuestionAnswerContent(ids.length));
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Map<Integer, Map<String, Integer>> container = unpackResult(dbutil.pd.getMapData(inData.get(Packet.Data.LoadQResults.RESULTS)));
		
		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestFewEntries() {
		int nEntries = 3;
		List<Integer> questionIDs = Arrays.asList(1, 2, 4);
		
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR);
		out.put(Packet.DATA, createDataPacket(createQuestionsRequestEntry(questionIDs), createDetailsEntry()).toString());
		
		setupDatabaseCalls(createDatabaseQuestionAnswerContent(nEntries));
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		Map<Integer, Map<String, Integer>> container = unpackResult(dbutil.pd.getMapData(inData.get(Packet.Data.LoadQResults.RESULTS)));
		
		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}

}
