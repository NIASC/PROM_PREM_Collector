package se.nordicehealth.servlet.implementation.io;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.implementation.Packet;
import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.implementation.io.ListData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.io.PacketData;

public class PacketDataTest {
	PacketData pd;
	LoggerForTesting logger;

	@Before
	public void setUp() throws Exception {
		logger = new LoggerForTesting();
		pd = new PacketData(new JSONParser(), logger);
	}

	@Test
	public void testGetMapData() {
		MapData md1 = new MapData(new JSONObject());
		md1.put(Packet.TYPE, "ping");
		MapData md2 = new MapData(new JSONObject());
		md2.put(Packet.Data.__NULL__, "test");
		md1.put(Packet.DATA, md2.toString());
		
		MapData _md1 = pd.getMapData(md1.toString());
		Assert.assertEquals(md1.toString(), _md1.toString());
		MapData _md2 = pd.getMapData(_md1.get(Packet.DATA));
		Assert.assertEquals(md2.toString(), _md2.toString());
	}

	@Test
	public void testGetListData() {
		ListData ld1 = new ListData(new JSONArray());
		ld1.add("ping");
		ListData ld2 = new ListData(new JSONArray());
		ld2.add("test");
		ld1.add(ld2.toString());
		
		ListData _ld1 = pd.getListData(ld1.toString());
		Assert.assertEquals(ld1.toString(), _ld1.toString());
		List<String> l = new ArrayList<String>();
		for (String str : _ld1.iterable()) { l.add(str); }
		ListData _ld2 = pd.getListData(l.get(1));
		Assert.assertEquals(ld2.toString(), _ld2.toString());
	}

}
