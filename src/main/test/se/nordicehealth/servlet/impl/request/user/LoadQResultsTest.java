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
	UserRequestUtil requtil;
	Map<Integer, Map<String, Integer>> allResults;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		processer = new LoadQResults(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.qdbf, dbutil.crypto);
		
		allResults = calculateStatistics();
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
		dataOut.put(Packet.QUESTIONS, questionIDs.toString());
		dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));
		dataOut.put(Packet.BEGIN, begin);
		dataOut.put(Packet.END, end);
		return dataOut;
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
		MapData dataOut = createDataPacket(createQuestionsRequestEntry(questionIDs),
				requtil.createUserUIDEntry(requtil.login()));
		List<Map<String, String>> dates = createDatabaseQuestionAnswerContent(ids.length);
		Map<Integer, Map<String, Integer>> container = sendRequest(dataOut, dates);

		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestInvalidUser() {
		List<Integer> questionIDs = Arrays.asList(1, 2, 4);
		MapData dataOut = createDataPacket(createQuestionsRequestEntry(questionIDs),
				new MapData());
		List<Map<String, String>> dates = createDatabaseQuestionAnswerContent(ids.length);
		Map<Integer, Map<String, Integer>> container = sendRequest(dataOut, dates);

		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestFewEntries() {
		int nEntries = 3;
		List<Integer> questionIDs = Arrays.asList(1, 2, 4);
		MapData dataOut = createDataPacket(createQuestionsRequestEntry(questionIDs),
				requtil.createUserUIDEntry(requtil.login()));
		List<Map<String, String>> dates = createDatabaseQuestionAnswerContent(nEntries);
		Map<Integer, Map<String, Integer>> container = sendRequest(dataOut, dates);

		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}
	
	@Test
	public void testProcessRequestEmptyList() {
		List<Integer> questionIDs = new ArrayList<Integer>();
		MapData dataOut = createDataPacket(createQuestionsRequestEntry(questionIDs),
				requtil.createUserUIDEntry(requtil.login()));
		List<Map<String, String>> dates = createDatabaseQuestionAnswerContent(ids.length);
		Map<Integer, Map<String, Integer>> container = sendRequest(dataOut, dates);

		Map<Integer, Map<String, Integer>> expected = new HashMap<Integer, Map<String, Integer>>();
		for (int i : questionIDs) {
			expected.put(i, allResults.get(i));
		}
		Assert.assertEquals(expected, container);
	}
	
	public Map<Integer, Map<String, Integer>> sendRequest(MapData dataOut, List<Map<String, String>> dates) {
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.LOAD_QR);
		out.put(Packet.DATA, dataOut.toString());

		requtil.setNextDatabaseUserCall();
		requtil.setNextDatabaseDatesMapStrStr(dates);
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		return unpackResult(dbutil.pd.getMapData(inData.get(Packet.RESULTS)));
	}

}
