package se.nordicehealth.servlet.impl.mail.emails;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import se.nordicehealth.servlet.impl.mail.MessageGenerator;

public class RegistrationRequest {

	public RegistrationRequest(MessageGenerator msggen) {
		this.msggen = msggen;
	}
	
	public EMail create(String name, String email, String clinic) {
		String emailSubject = "PROM/PREM: Registration request";
		Map<String, String> tokens = new LinkedHashMap<String, String>();
		tokens.put("PPC_REGREQ_NAME", name);
		tokens.put("PPC_REGREQ_EMAIL", email);
		tokens.put("PPC_REGREQ_CLINIC", clinic);
		return new EMail(new ArrayList<String>(0), emailSubject, msggen.generate(tokens), "text/html; charset=utf-8");
	}

	private MessageGenerator msggen;
}
