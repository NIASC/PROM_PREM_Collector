package se.nordicehealth.servlet.impl.request.admin;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class _AddClinic extends RequestProcesser {
	private PPCDatabase db;
	
	public _AddClinic(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._ADD_CLINIC);

		MapData data = packetData.getMapData();
		String result = AdminPacket.FAIL;
		try {
			if (_storeClinic(packetData.getMapData(in.get(AdminPacket._DATA)))) {
				result = AdminPacket.SUCCESS;
			}
		} catch (Exception e) { }
		data.put(AdminPacket.RESPONSE, result);

		out.put(AdminPacket._DATA, data.toString());
		return out;
	}
	
	private boolean _storeClinic(MapData in) throws Exception {
		String name = in.get(AdminPacket.NAME);
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		return db.addClinic(name);
	}
}