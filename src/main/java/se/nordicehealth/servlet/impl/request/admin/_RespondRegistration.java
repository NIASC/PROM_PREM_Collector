package se.nordicehealth.servlet.impl.request.admin;

import static se.nordicehealth.servlet.impl.AdminPacket._DATA;
import static se.nordicehealth.servlet.impl.AdminPacket._TYPE;

import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
import se.nordicehealth.servlet.impl.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationResponse;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class _RespondRegistration extends RequestProcesser {
	
	public _RespondRegistration(IPacketData packetData, PPCLogger logger, MailMan emailer, RegistrationResponse resp) {
		super(packetData, logger);
		this.emailer = emailer;
		this.resp = resp;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.RSP_REGISTR);

		MapData data = packetData.getMapData();
		AdminData.AdminRespondRegistration.Response result = AdminData.AdminRespondRegistration.Response.FAIL;
		try {
			if (_sendRegResp(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminRespondRegistration.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminRespondRegistration.RESPONSE, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private MailMan emailer;
	private RegistrationResponse resp;
	
	private boolean _sendRegResp(MapData in) throws Exception {
		MapData details = packetData.getMapData(in.get(AdminData.AdminRespondRegistration.DETAILS));
		String username = details.get(AdminData.AdminRespondRegistration.Details.USERNAME);
		if (!validString(username)) {
			return false;
		}
		String password = details.get(AdminData.AdminRespondRegistration.Details.PASSWORD);
		if (!validString(password)) {
			return false;
		}
		String email = details.get(AdminData.AdminRespondRegistration.Details.EMAIL);
		if (!validString(email) || !email.contains("@")) {
			return false;
		}
		return emailer.send(resp.create(username, password, email));
	}
	
	private boolean validString(String str) {
		return str != null && !str.trim().isEmpty();
	}
}