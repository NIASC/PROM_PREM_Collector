package se.nordicehealth.servlet.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static se.nordicehealth.common.impl.Packet.TYPE;
import static se.nordicehealth.servlet.impl.AdminPacket._ADMIN;
import static se.nordicehealth.servlet.impl.AdminPacket._TYPE;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet.Types;
import se.nordicehealth.servlet.core.PPCClientRequestProcesser;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.core.usermanager.UserManager;
import se.nordicehealth.servlet.impl.AdminPacket.Admin;
import se.nordicehealth.servlet.impl.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class ClientRequestProcesser implements PPCClientRequestProcesser {
	
	public ClientRequestProcesser(PPCLogger logger, IPacketData packetData, PPCUserManager um,
			Map<Types, RequestProcesser> userMethods, Map<AdminTypes, RequestProcesser> adminMethods) {
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
			boolean admin = false;
			if (obj.get(_ADMIN) != null) {
				try {
					admin = Constants.equal(Admin.YES, Constants.getEnum(Admin.values(), obj.get(_ADMIN)));
				} catch (NumberFormatException ignored) { }
			}
			RequestProcesser rp = extractRequestType(obj, admin, remoteAddr, hostAddr);
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

	private RequestProcesser extractRequestType(MapData obj, boolean adminRequest, String remoteAddr, String hostAddr) throws SecurityException {
		if (adminRequest) {
			if (!remoteAddr.equals(hostAddr)) {
				throw new SecurityException("Request was of type admin but host and remote adress did not match");
			}
			return adminMethods.get(Constants.getEnum(AdminTypes.values(), obj.get(_TYPE)));
		} else {
			return userMethods.get(Constants.getEnum(Types.values(), obj.get(TYPE)));
		}
	}
	
	private PPCLogger logger;
	private IPacketData packetData;
	private PPCUserManager um;
	private Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
	private Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
}
