package se.nordicehealth.servlet.impl.request.user;

import static se.nordicehealth.common.impl.Packet.DATA;
import static se.nordicehealth.common.impl.Packet.TYPE;

import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.common.impl.Packet.Types;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.Crypto;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationRequest;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class RequestRegistration extends RequestProcesser {
	private Crypto crypto;
	
	public RequestRegistration(PPCDatabase db, IPacketData packetData, PPCLogger logger, MailMan emailer, RegistrationRequest req, Crypto crypto) {
		super(packetData, logger);
		this.emailer = emailer;
		this.req = req;
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(TYPE, Types.REQ_REGISTR);

		MapData data = packetData.getMapData();
		Data.RequestRegistration.Response result = Data.RequestRegistration.Response.FAIL;
		try {
			if (sendRegistration(packetData.getMapData(in.get(DATA)))) {
				result = Data.RequestRegistration.Response.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(Data.RequestRegistration.RESPONSE, result);

		out.put(DATA, data.toString());
		return out;
	}
	
	private MailMan emailer;
	private RegistrationRequest req;
	
	private boolean sendRegistration(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Data.RequestRegistration.DETAILS)));
		String name = inpl.get(Data.RequestRegistration.Details.NAME);
		String email = inpl.get(Data.RequestRegistration.Details.EMAIL);
		String clinic = inpl.get(Data.RequestRegistration.Details.CLINIC);
		return emailer.send(req.create(name, email, clinic));
	}
}