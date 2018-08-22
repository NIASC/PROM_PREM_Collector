package se.nordicehealth.servlet.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCClientRequestProcesser;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class ClientRequestProcesser implements PPCClientRequestProcesser {
	
	public ClientRequestProcesser(PPCLogger logger, IPacketData packetData, PPCUserManager um,
			Map<String, RequestProcesser> userMethods, Map<String, RequestProcesser> adminMethods) {
		this.logger = logger;
		this.packetData = packetData;
		this.um = um;
		this.userMethods = userMethods;
		this.adminMethods = adminMethods;
		
		um.startManagement();
	}
	
	@Override
	public String handleRequest(String message, String remoteAddr, String hostAddr) {
		try {
			MapData obj = packetData.getMapData(message);
			RequestProcesser rp = extractRequestType(obj, remoteAddr, hostAddr);
			return rp.processRequest(obj).toString();
		} catch (Exception e) {
			logger.log(Level.INFO, "Unknown request", e);
			return packetData.getMapData().toString();
		}
	}
	
	@Override
	public void terminate() {
		um.stopManagement();
	}

	private RequestProcesser extractRequestType(MapData obj, String remoteAddr, String hostAddr) throws SecurityException {
		if (obj.get(AdminPacket._TYPE) != null) {
			if (!remoteAddr.equals(hostAddr)) {
				throw new SecurityException("Request was of type admin but host and remote adress did not match");
			}
			return adminMethods.get(obj.get(AdminPacket._TYPE));
		} else {
			return userMethods.get(obj.get(Packet.TYPE));
		}
	}
	
	private PPCLogger logger;
	private IPacketData packetData;
	private PPCUserManager um;
	private Map<String, RequestProcesser> userMethods = new HashMap<String, RequestProcesser>();
	private Map<String, RequestProcesser> adminMethods = new HashMap<String, RequestProcesser>();
}
