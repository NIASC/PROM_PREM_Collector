package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.mail.MailMan;
import servlet.implementation.mail.emails.RegistrationResponse;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class _RespondRegistration extends RequestProcesser {
	
	public _RespondRegistration(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger, MailMan emailer, RegistrationResponse resp) {
		super(um, db, packetData, qdbf, logger);
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
		String password = details.get(AdminData.AdminRespondRegistration.Details.PASSWORD);
		String email = details.get(AdminData.AdminRespondRegistration.Details.EMAIL);
		return emailer.send(resp.create(username, password, email));
	}
}