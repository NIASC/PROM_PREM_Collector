package se.nordicehealth.servlet.impl;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.impl.LocaleSE;

public class LocaleSETest {
	LocaleSE locale;

	@Before
	public void setUp() throws Exception {
		locale = new LocaleSE();
	}
	
	@Test
	public void testCalculateControlNumber() {
		String pID = "640823-3234";
		Assert.assertEquals(4, locale.calculateControlDigit(640823323));
		Assert.assertNotEquals(4, locale.calculateControlDigit(640823322));
	}

	@Test
	public void testFormatValidPersonalID() {
		try {
			String expected = "19640823-3234";
			Assert.assertEquals(expected, locale.formatPersonalID("6408233234"));
			Assert.assertEquals(expected, locale.formatPersonalID("640823-3234"));
			Assert.assertEquals(expected, locale.formatPersonalID("196408233234"));
			Assert.assertEquals(expected, locale.formatPersonalID("19640823-3234"));
		} catch (ParseException e) {
			Assert.fail("Personal id was properly formatted but could not be parsed");
		}
	}
	
	@Test
	public void testFormatTooLongPersonalID() {
		try {
			String str = locale.formatPersonalID("70001011237");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("7000101-1237");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("1970001011237");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("197000101-1237");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
	}
	
	@Test
	public void testFormatTooShortPersonalID() {
		try {
			String str = locale.formatPersonalID("701011233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("70101-1233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("19701011233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("1970101-1233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
	}
	
	@Test
	public void testFormatInvalidDatePersonalID() {
		try {
			String str = locale.formatPersonalID("7001321233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("700132-1233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("197001321233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("19700132-1233");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
	}
	
	@Test
	public void testFormatInvalidStringsPersonalID() {
		try {
			String str = locale.formatPersonalID("asdasd");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
		try {
			String str = locale.formatPersonalID("");
			Assert.fail("Personal id was badly formatted but it was successfully parsed");
		} catch (ParseException e) { }
	}
}
