package niasc.servlet.implementation.mail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import servlet.implementation.mail.MessageGenerator;

public class MessageGeneratorTest {
	MessageGenerator msggen;
	String templ = "Hello NAME! Are you MOOD?";

	@Before
	public void setUp() throws Exception {
		msggen = new MessageGenerator(templ);
	}

	@Test
	public void testGenerate() {
		String result = "Hello John! Are you happy?";
		Map<String, String> tokens = new LinkedHashMap<String, String>(2);
		tokens.put("NAME", "John");
		tokens.put("MOOD", "happy");
		Assert.assertEquals(result, msggen.generate(tokens));
	}

	@Test
	public void testGenerateNullReplacement() {
		Map<String, String> tokens = new LinkedHashMap<String, String>(2);
		tokens.put(null, "John");
		tokens.put("MOOD", "happy");
		try {
			String str = msggen.generate(tokens);
			Assert.fail("null token was successfully replaced. '" + str + "' was generated.");
		} catch (NullPointerException e) { }
	}

	@Test
	public void testGenerateNullToken() {
		Map<String, String> tokens = new LinkedHashMap<String, String>(2);
		tokens.put("NAME", null);
		tokens.put("MOOD", "happy");
		try {
			String str = msggen.generate(tokens);
			Assert.fail("token was successfully replaced with null. '" + str + "' was generated.");
		} catch (NullPointerException e) { }
	}

}
