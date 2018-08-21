package se.nordicehealth.servlet.impl.mail;

import java.util.Map;
import java.util.Map.Entry;

public class MessageGenerator {
	
	public MessageGenerator(String template) {
		setTemplate(template);
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String generate(Map<String, String> replaceTokens) {
		String out = template;
		for (Entry<String, String> e : replaceTokens.entrySet()) {
			out = out.replace(e.getKey(), e.getValue());
		}
		return out;
	}
	
	private String template;
}
