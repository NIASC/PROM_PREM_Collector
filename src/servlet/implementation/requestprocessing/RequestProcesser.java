package servlet.implementation.requestprocessing;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.UserManager;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;

public abstract class RequestProcesser {
	protected UserManager um;
	protected Database db;
	protected PacketData packetData;
	protected QDBFormat qdbf;
	protected _Logger logger;
	
	public RequestProcesser(UserManager um, Database db, PacketData packetData, QDBFormat qdbf, _Logger logger) {
		this.um = um;
		this.db = db;
		this.packetData = packetData;
		this.qdbf = qdbf;
		this.logger = logger;
	}
	
	public abstract MapData processRequest(MapData in) throws Exception;
}
