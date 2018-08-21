package se.nordicehealth.servlet.impl.request.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.user.LoadQResultDates;

public class LoadQResultDatesTest {
	LoadQResultDates processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		processer = new LoadQResultDates(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.crypto);
	}

	@Test
	public void testProcessRequest() {
		MapData details = requtil.createUserUIDEntry(requtil.login());
		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResultDates.DETAILS, dbutil.crypto.encrypt(details.toString()));
		List<String> datesOut = Arrays.asList("1970-01-01", "1999-12-31", "2015-10-15", "1985-10-15");
		List<String> datesIn = sendRequest(dataOut, datesOut);
		Assert.assertEquals(datesOut, datesIn);
	}

	@Test
	public void testProcessRequestNoDates() {
		MapData details = requtil.createUserUIDEntry(requtil.login());
		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResultDates.DETAILS, dbutil.crypto.encrypt(details.toString()));
		List<String> datesOut = Arrays.asList();
		List<String> datesIn = sendRequest(dataOut, datesOut);
		Assert.assertEquals(datesOut, datesIn);
	}

	@Test
	public void testProcessRequestUserNotOnline() {
		MapData details = requtil.createUserUIDEntry(0L);
		MapData dataOut = new MapData();
		dataOut.put(Packet.Data.LoadQResultDates.DETAILS, dbutil.crypto.encrypt(details.toString()));
		List<String> datesOut = Arrays.asList("1970-01-01", "1999-12-31", "2015-10-15", "1985-10-15");
		List<String> datesIn = sendRequest(dataOut, datesOut);
		Assert.assertNotEquals(datesOut, datesIn);
	}

	public List<String> sendRequest(MapData dataOut, List<String> datesOut) {
		MapData out = new MapData();
		out.put(Packet.TYPE, Packet.Types.LOAD_QR_DATE);
		out.put(Packet.DATA, dataOut.toString());
		requtil.setNextDatabaseUserCall();
		requtil.setNextDatabaseDatesString(datesOut);
		
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		List<String> datesIn = new ArrayList<String>();
		ListData dlist = dbutil.pd.getListData(inData.get(Packet.Data.LoadQResultDates.DATES));
		for (String str : dlist.iterable()) {
			datesIn.add(str);
		}
		return datesIn;
	}

}
