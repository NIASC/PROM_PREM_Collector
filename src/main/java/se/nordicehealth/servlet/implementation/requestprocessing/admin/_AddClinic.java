package se.nordicehealth.servlet.implementation.requestprocessing.admin;

import static se.nordicehealth.servlet.implementation.AdminPacket._DATA;
import static se.nordicehealth.servlet.implementation.AdminPacket._TYPE;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminData;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.MapData;
import se.nordicehealth.servlet.implementation.requestprocessing.RequestProcesser;

public class _AddClinic extends RequestProcesser {
	private PPCDatabase db;
	
	public _AddClinic(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.ADD_CLINIC);

		MapData data = packetData.getMapData();
		AdminData.AdminAddClinic.Response result = AdminData.AdminAddClinic.Response.FAIL;
		try {
			if (_storeClinic(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminAddClinic.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminAddClinic.RESPONSE, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private boolean _storeClinic(MapData in) throws Exception {
		String name = in.get(AdminData.AdminAddClinic.NAME);
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		return db.addClinic(name);
	}
}