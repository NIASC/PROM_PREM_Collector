package se.nordicehealth.servlet.implementation.requestprocessing;

import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;

public abstract class RequestProcesser {
	protected IPacketData packetData;
	protected PPCLogger logger;
	
	public RequestProcesser(IPacketData packetData, PPCLogger logger) {
		this.packetData = packetData;
		this.logger = logger;
	}
	
	public abstract MapData processRequest(MapData in) throws Exception;
}
