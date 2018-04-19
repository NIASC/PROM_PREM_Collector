package servlet.implementation.requestprocessing;

import servlet.core.PPCLogger;
import servlet.implementation.io.MapData;
import servlet.implementation.io.IPacketData;

public abstract class RequestProcesser {
	protected IPacketData packetData;
	protected PPCLogger logger;
	
	public RequestProcesser(IPacketData packetData, PPCLogger logger) {
		this.packetData = packetData;
		this.logger = logger;
	}
	
	public abstract MapData processRequest(MapData in) throws Exception;
}
