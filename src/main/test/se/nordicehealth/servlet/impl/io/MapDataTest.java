package se.nordicehealth.servlet.impl.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.MapData;

public class MapDataTest {
	MapData md;
	String enumKey = Packet.TYPE;
	String enumVal = Packet.PING;
	String strVal = "ping";
	Integer intKey = 0;
	String strKey = "ping";
	Integer intVal = 0;
	

	@Before
	public void setUp() throws Exception {
		md = new MapData(new JSONObject());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testToString() {
		JSONObject o = new JSONObject();
		Assert.assertEquals(o.toString(), md.toString());
		o.put(intKey, strVal);
		md.put(intKey, strVal);
		Assert.assertEquals(o.toString(), md.toString());
		o.put(strKey, Integer.toString(intVal));
		md.put(strKey, intVal);
		Assert.assertEquals(o.toString(), md.toString());
	}

	@Test
	public void testPutEnumOfQEnumOfQ() {
		md.put(enumKey, enumVal);
		String val = md.get(enumKey);
		Assert.assertEquals(val, enumVal);
	}

	@Test
	public void testPutEnumOfQString() {
		md.put(enumKey, strVal);
		Assert.assertEquals(strVal, md.get(enumKey));
	}

	@Test
	public void testPutIntegerString() {
		md.put(intKey, strVal);
		Map<Integer, String> m = new HashMap<Integer, String>();
		for (Entry<String, String> e : md.iterable()) {
			m.put(Integer.parseInt(e.getKey()), e.getValue());
		}
		Assert.assertEquals(strVal, m.get(intKey));
	}

	@Test
	public void testPutStringInteger() {
		md.put(strKey, intVal);
		Map<String, Integer> m = new HashMap<String, Integer>();
		for (Entry<String, String> e : md.iterable()) {
			m.put(e.getKey(), Integer.parseInt(e.getValue()));
		}
		Assert.assertEquals(intVal, m.get(strKey));
	}

}
