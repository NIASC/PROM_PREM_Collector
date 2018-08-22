package se.nordicehealth.servlet.impl.request.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.admin._GetClinics;

public class GetClinicsTest {
	ReqProcUtil dbutil;
	
	_GetClinics processer;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		processer = new _GetClinics(dbutil.pd, dbutil.logger, dbutil.db);
		
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("id", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "phony");
		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNumberOfAvailableNextCalls(1);
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._GET_CLINICS);
		
		MapData out = processer.processRequest(in);
		MapData data = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		MapData clinics = dbutil.pd.getMapData(data.get(AdminPacket.CLINICS));
		Map<Integer, String> _clinics = new HashMap<Integer, String>();
		for (Entry<String, String> e : clinics.iterable()) {
			_clinics.put(Integer.parseInt(e.getKey()), e.getValue());
		}
		Assert.assertEquals(1, _clinics.size());
		Assert.assertTrue(_clinics.containsKey(1));
		Assert.assertEquals("phony", _clinics.get(1));
	}

}
