package servlet.implementation.requestprocessing.user;

import static common.implementation.Packet.DATA;
import static common.implementation.Packet.TYPE;

import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.Crypto;
import servlet.implementation.MailMan;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;

public class RequestRegistration extends RequestProcesser {
	
	public RequestRegistration(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, ServletLogger logger) {
		super(um, db, packetData, qdbf, logger);
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.REQ_REGISTR);

		MapData data = packetData.getMapData();
		Data.RequestRegistration.Response result = Data.RequestRegistration.Response.FAIL;
		try {
			if (sendRegistration(packetData.getMapData(in.get(DATA)))) { result = Data.RequestRegistration.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(Data.RequestRegistration.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private boolean sendRegistration(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestRegistration.DETAILS)));
		String name = inpl.get(Data.RequestRegistration.Details.NAME);
		String email = inpl.get(Data.RequestRegistration.Details.EMAIL);
		String clinic = inpl.get(Data.RequestRegistration.Details.CLINIC);
		return MailMan.sendRegReq(name, email, clinic);
	}
}