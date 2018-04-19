package servlet.implementation;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import common.implementation.Constants;
import static common.implementation.Packet.TYPE;
import static servlet.implementation.AdminPacket._ADMIN;
import static servlet.implementation.AdminPacket._TYPE;

import common.implementation.Packet.Types;
import servlet.core.PPCLogger;
import servlet.core.PPCClientRequestProcesser;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.Admin;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

public class ClientRequestProcesser implements PPCClientRequestProcesser {
	
	public ClientRequestProcesser(PPCLogger logger, IPacketData packetData, UserManager um,
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
	private UserManager um;
	private Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
	private Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
}
