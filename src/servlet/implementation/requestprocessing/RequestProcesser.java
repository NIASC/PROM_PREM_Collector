package servlet.implementation.requestprocessing;

import servlet.core._Logger;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;

public abstract class RequestProcesser {
	protected _PacketData packetData;
	protected _Logger logger;
	
	public RequestProcesser(_PacketData packetData, _Logger logger) {
		this.packetData = packetData;
		this.logger = logger;
	}
	
	public abstract MapData processRequest(MapData in) throws Exception;
}
