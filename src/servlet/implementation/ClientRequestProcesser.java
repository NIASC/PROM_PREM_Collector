package servlet.implementation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import common.implementation.Constants;
import static common.implementation.Packet.TYPE;
import static servlet.implementation.AdminPacket._ADMIN;
import static servlet.implementation.AdminPacket._TYPE;

import common.implementation.Packet.Types;
import servlet.core._Logger;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.Admin;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

public class ClientRequestProcesser {
	
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
	
	public ClientRequestProcesser(_Logger logger, _PacketData packetData, UserManager um,
			Map<Types, RequestProcesser> userMethods, Map<AdminTypes, RequestProcesser> adminMethods) {
		this.logger = logger;
		this.packetData = packetData;
		this.um = um;
		this.userMethods = userMethods;
		this.adminMethods = adminMethods;
		
		um.startManagement();
	}
	
	public void terminate() {
		um.stopManagement();
	}
	
	private _Logger logger;
	private _PacketData packetData;
	private UserManager um;
	private Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
	private Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
}
