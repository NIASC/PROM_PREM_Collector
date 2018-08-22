package se.nordicehealth.servlet.impl.request.admin;

import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket;
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
		out.put(AdminPacket._TYPE, AdminPacket._RSP_REGISTR);

		MapData data = packetData.getMapData();
		String result = AdminPacket.FAIL;
		try {
			if (_sendRegResp(packetData.getMapData(in.get(AdminPacket._DATA)))) {
				result = AdminPacket.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(AdminPacket.RESPONSE, result);

		out.put(AdminPacket._DATA, data.toString());
		return out;
	}
	
	private MailMan emailer;
	private RegistrationResponse resp;
	
	private boolean _sendRegResp(MapData in) throws Exception {
		MapData details = packetData.getMapData(in.get(AdminPacket.DETAILS));
		String username = details.get(AdminPacket.USERNAME);
		if (!validString(username)) {
			return false;
		}
		String password = details.get(AdminPacket.PASSWORD);
		if (!validString(password)) {
			return false;
		}
		String email = details.get(AdminPacket.EMAIL);
		if (!validString(email) || !email.contains("@")) {
			return false;
		}
		return emailer.send(resp.create(username, password, email));
	}
	
	private boolean validString(String str) {
		return str != null && !str.trim().isEmpty();
	}
}