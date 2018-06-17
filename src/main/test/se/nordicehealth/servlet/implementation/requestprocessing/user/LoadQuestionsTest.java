package se.nordicehealth.servlet.implementation.requestprocessing.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.implementation.Packet;
import se.nordicehealth.common.implementation.Packet.Data;
import se.nordicehealth.servlet.implementation.io.MapData;

public class LoadQuestionsTest {
	LoadQuestions processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		processer = new LoadQuestions(dbutil.pd, dbutil.logger, dbutil.db);
	}
	
	private int setNextDatabaseQuestions() {
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("option0", "yes");
		strings.put("option1", "no");
		strings.put("option2", "none");
		strings.put("type", "SingleOption");
		strings.put("question", "Yes or no");
		strings.put("description", "Answer yes or no");
		
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("id", 0);
		ints.put("optional", 0);
		ints.put("max_val", 0);
		ints.put("min_val", 0);

		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNextInts(ints);
		return 1;
	}

	@Test
	public void testProcessRequest() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_Q);
		
		dbutil.rs.setNumberOfAvailableNextCalls(setNextDatabaseQuestions());
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));
		MapData allQData = dbutil.pd.getMapData(inData.get(Data.LoadQuestions.QUESTIONS));
		int nEntries = 0;
		for (Entry<String, String> e : allQData.iterable()) {
			++nEntries;
			MapData qData = dbutil.pd.getMapData(e.getValue());
			
			List<String> q = new ArrayList<String>();
			for (Entry<String, String> _e : qData.iterable()) {
				q.add(_e.getValue());
			}
			Assert.assertTrue(q.contains("Answer yes or no"));
		}
		Assert.assertEquals(1, nEntries);
	}
}
