package se.nordicehealth.servlet.implementation.mail.emails;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import se.nordicehealth.servlet.implementation.mail.MessageGenerator;

public class RegistrationResponse {

	public RegistrationResponse(MessageGenerator msggen) {
		this.msggen = msggen;
	}
	
	public EMail create(String username, String password, String email) {
		String emailSubject = "PROM/PREM: Registration response";
		Map<String, String> tokens = new LinkedHashMap<String, String>();
		tokens.put("PPC_REGRESP_USERNAME", username);
		tokens.put("PPC_REGRESP_PASSWORD", password);
		return new EMail(Arrays.asList(email), emailSubject, msggen.generate(tokens), "text/html; charset=utf-8");
	}

	MessageGenerator msggen;
}
