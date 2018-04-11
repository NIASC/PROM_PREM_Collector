package niasc.servlet.implementation.io;

import static org.junit.Assert.*;

import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import servlet.implementation.io.PacketData;

public class PacketDataTest {
	PacketData pd;

	@Before
	public void setUp() throws Exception {
		pd = new PacketData(new JSONParser());
	}

	@Test
	public void testGetMapData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMapDataString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetListData() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetListDataString() {
		fail("Not yet implemented");
	}

}
