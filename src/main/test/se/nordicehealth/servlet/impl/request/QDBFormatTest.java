package se.nordicehealth.servlet.impl.request;

import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants.QuestionTypes;
import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.core.stats.containers.Area;
import se.nordicehealth.servlet.core.stats.containers.MultipleOption;
import se.nordicehealth.servlet.core.stats.containers.SingleOption;
import se.nordicehealth.servlet.core.stats.containers.Slider;
import se.nordicehealth.servlet.core.stats.containers.Statistics;
import se.nordicehealth.servlet.impl.MySQLDatabase;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.io.PacketData;
import se.nordicehealth.servlet.impl.request.QDBFormat;
import se.nordicehealth.zzphony.database.PhonyConnection;
import se.nordicehealth.zzphony.database.PhonyDataSource;
import se.nordicehealth.zzphony.database.PhonyResultSet;
import se.nordicehealth.zzphony.database.PhonyStatement;

public class QDBFormatTest {
	QDBFormat fmt;
	IPacketData pd;
	LoggerForTesting logger;

	MySQLDatabase db;
	PhonyDataSource ds;
	PhonyStatement s;
	PhonyResultSet rs;

	@Before
	public void setUp() throws Exception {
		logger = new LoggerForTesting();
		
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		ds = new PhonyDataSource(new PhonyConnection(s));
		db = new MySQLDatabase(ds, null, logger);
		
		pd = new PacketData(new JSONParser(), logger);
		fmt = new QDBFormat(db, pd);
	}

	@Test
	public void testGetDBFormatEmpty() throws Exception {
		MapData data = pd.getMapData();
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("''", dbfmt);
	}

	@Test
	public void testGetDBFormatAreaNormal() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.AREA, "message");
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("'message'", dbfmt);
	}

	@Test
	public void testGetDBFormatAreaWithQuotes() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.AREA, "'message'");
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("'\"message\"'", dbfmt);
	}

	@Test
	public void testGetDBFormatSlider() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.SLIDER, "5");
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("'slider5'", dbfmt);
	}

	@Test
	public void testGetDBFormatSliderWithText() {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.SLIDER, "a");
		try {
			fmt.getDBFormat(data);
			Assert.fail("Slider value is not a number, should throw exception");
		} catch (Exception e) {
			
		}
	}

	@Test
	public void testGetDBFormatMultipleOption() throws Exception {
		MapData data = pd.getMapData();
		ListData options = pd.getListData();
		options.add("1");
		options.add("3");
		data.put(QuestionTypes.MULTIPLE_OPTION, options.toString());
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("[\"option1\",\"option3\"]", dbfmt);
	}

	@Test
	public void testGetDBFormatMultipleOptionWithMalformedOptions() throws Exception {
		MapData data = pd.getMapData();
		ListData options = pd.getListData();
		options.add("1");
		options.add("a");
		options.add("");
		data.put(QuestionTypes.MULTIPLE_OPTION, options.toString());
		try {
			fmt.getDBFormat(data);
			Assert.fail("Malformed options. Should trow excetion");
		} catch (Exception e) { }
	}

	@Test
	public void testGetDBFormatMultipleOptionWithSingleOptionEntry() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.MULTIPLE_OPTION, "1");
		try {
			fmt.getDBFormat(data);
			Assert.fail("MultipleOption entry is not a ListData object. Should throw exception.");
		} catch (Exception e) { }
	}

	@Test
	public void testGetDBFormatSingleOption() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.SINGLE_OPTION, "1");
		String dbfmt = fmt.getDBFormat(data);
		Assert.assertEquals("'option1'", dbfmt);
	}

	@Test
	public void testGetDBFormatSingleOptionMalformedExpression() throws Exception {
		MapData data = pd.getMapData();
		data.put(QuestionTypes.SINGLE_OPTION, "a");
		try {
			fmt.getDBFormat(data);
			Assert.fail("Malformed expression. Should throw exception.");
		} catch (Exception e) { }
	}

	@Test
	public void testGetDBFormatSingleOptionWithMultipleOptionEntry() throws Exception {
		MapData data = pd.getMapData();
		ListData options = pd.getListData();
		options.add("1");
		options.add("3");
		data.put(QuestionTypes.SINGLE_OPTION, options.toString());
		try {
			fmt.getDBFormat(data);
			Assert.fail("MultipleOption was passed as SingleOption. Should throw exception");
		} catch (Exception e) { }
	}

	@Test
	public void testGetQFormatSlider() {
		Statistics s = fmt.getQFormat(1, "slider1");
		Assert.assertEquals(1, s.question());
		Assert.assertTrue(s instanceof Slider);
	}

	@Test
	public void testGetQFormatSingleOption() {
		Statistics s = fmt.getQFormat(1, "option1");
		Assert.assertEquals(1, s.question());
		Assert.assertTrue(s instanceof SingleOption);
	}

	@Test
	public void testGetQFormatMultipleOptionOption() {
		Statistics s = fmt.getQFormat(1, "[\"option1\",\"option3\"]");
		Assert.assertEquals(1, s.question());
		Assert.assertTrue(s instanceof MultipleOption);
	}
	
	@Test
	public void testGetQFormatArea() {
		Statistics s = fmt.getQFormat(1, "\"A string message.\"");
		Assert.assertEquals(1, s.question());
		Assert.assertTrue(s instanceof Area);
	}

}
