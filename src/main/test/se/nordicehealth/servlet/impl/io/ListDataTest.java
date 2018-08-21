package se.nordicehealth.servlet.impl.io;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.io.ListData;

import org.json.simple.JSONArray;

public class ListDataTest {
	ListData ld;
	String e1 = "entry1";
	String e2 = "entry2";

	@Before
	public void setUp() throws Exception {
		ld = new ListData(new JSONArray());
	}

	@Test
	public void testToString() {
		JSONArray jarr = new JSONArray();
		Assert.assertEquals(jarr.toString(), ld.toString());
		jarr.add(e1); ld.add(e1);
		Assert.assertEquals(jarr.toString(), ld.toString());
		jarr.add(e2); ld.add(e2);
		Assert.assertEquals(jarr.toString(), ld.toString());
	}

	@Test
	public void testAddAndCheckContent() {
		ld.add(e1);
		List<String> l = new ArrayList<String>();
		for (String s : ld.iterable()) { l.add(s); }
		Assert.assertEquals(e1, l.get(0));
	}

}
