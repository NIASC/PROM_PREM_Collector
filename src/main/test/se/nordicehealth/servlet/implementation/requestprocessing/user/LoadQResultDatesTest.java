package se.nordicehealth.servlet.implementation.requestprocessing.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.implementation.Packet;
import se.nordicehealth.servlet.implementation.io.ListData;
import se.nordicehealth.servlet.implementation.io.MapData;

public class LoadQResultDatesTest {
	LoadQResultDates processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		
		processer = new LoadQResultDates(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.crypto);
	}
	
	private int setNextDatabaseUserCall(String name) {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", name);
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		dbutil.rs.setNextStrings(strings);
		return 1;
	}
	
	private int setNextDatabaseDates(List<String> dates) {
		for (String date : dates) {
			Map<String, String> strings = new HashMap<String, String>();
			strings.put("date", date);
			dbutil.rs.setNextStrings(strings);
			dbutil.rs.setNextInts(new HashMap<String, Integer>());
		}
		return dates.size();
	}
	
	private MapData createRequest(long uid) {
		MapData details = new MapData();
		details.put(Packet.Data.LoadQResultDates.Details.UID, Long.toString(uid));

		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResultDates.DETAILS, dbutil.crypto.encrypt(details.toString()));
		return dataOut;
	}
	
	private void setupDatabaseCalls(List<String> dates) {
		int availNextCalls = 0;
		availNextCalls += setNextDatabaseUserCall("phony");
		availNextCalls += setNextDatabaseDates(dates);
		dbutil.rs.setNumberOfAvailableNextCalls(availNextCalls);
	}

	@Test
	public void testProcessRequest() {
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR_DATE);
		out.put(Packet.DATA, createRequest(1L).toString());

		List<String> datesOut = Arrays.asList("1970-01-01", "1999-12-31", "2015-10-15", "1985-10-15");
		setupDatabaseCalls(datesOut);
		
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		List<String> datesIn = new ArrayList<String>();
		ListData dlist = dbutil.pd.getListData(inData.get(Packet.Data.LoadQResultDates.DATES));
		for (String str : dlist.iterable()) {
			datesIn.add(str);
		}
		Assert.assertEquals(datesOut, datesIn);
	}

	@Test
	public void testProcessRequestNoDates() {
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR_DATE);
		out.put(Packet.DATA, createRequest(1L).toString());

		List<String> datesOut = Arrays.asList();
		setupDatabaseCalls(datesOut);
		
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		List<String> datesIn = new ArrayList<String>();
		ListData dlist = dbutil.pd.getListData(inData.get(Packet.Data.LoadQResultDates.DATES));
		for (String str : dlist.iterable()) {
			datesIn.add(str);
		}
		Assert.assertEquals(datesOut, datesIn);
	}

	@Test
	public void testProcessRequestUserNotOnline() {
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR_DATE);
		
		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResultDates.DETAILS, dbutil.crypto.encrypt(new MapData().toString()));
		out.put(Packet.DATA, dataOut.toString());

		List<String> datesOut = Arrays.asList("1970-01-01", "1999-12-31", "2015-10-15", "1985-10-15");
		setupDatabaseCalls(datesOut);
		
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		List<String> datesIn = new ArrayList<String>();
		ListData dlist = dbutil.pd.getListData(inData.get(Packet.Data.LoadQResultDates.DATES));
		for (String str : dlist.iterable()) {
			datesIn.add(str);
		}
		Assert.assertNotEquals(datesOut, datesIn);
	}

}
