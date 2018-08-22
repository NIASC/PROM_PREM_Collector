package se.nordicehealth.servlet.impl.request.user;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationRequest;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class RequestRegistration extends RequestProcesser {
	private PPCEncryption crypto;
	
	public RequestRegistration(PPCDatabase db, IPacketData packetData, PPCLogger logger, MailMan emailer, RegistrationRequest req, PPCEncryption crypto) {
		super(packetData, logger);
		this.emailer = emailer;
		this.req = req;
		this.crypto = crypto;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(Packet.TYPE, Packet.REQ_REGISTR);

		MapData data = packetData.getMapData();
		String result = Packet.FAIL;
		try {
			if (sendRegistration(packetData.getMapData(in.get(Packet.DATA)))) {
				result = Packet.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(Packet.RESPONSE, result);

		out.put(Packet.DATA, data.toString());
		return out;
	}
	
	private MailMan emailer;
	private RegistrationRequest req;
	
	private boolean sendRegistration(MapData in) throws Exception {
		MapData inpl = packetData.getMapData(crypto.decrypt(in.get(Packet.DETAILS)));
		String name = inpl.get(Packet.NAME);
		String email = inpl.get(Packet.EMAIL);
		String clinic = inpl.get(Packet.CLINIC);
		return emailer.send(req.create(name, email, clinic));
	}
}