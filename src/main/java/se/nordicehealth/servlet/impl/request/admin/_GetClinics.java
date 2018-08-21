package se.nordicehealth.servlet.impl.request.admin;

import static se.nordicehealth.servlet.impl.AdminPacket._DATA;
import static se.nordicehealth.servlet.impl.AdminPacket._TYPE;

import java.util.Map;
import java.util.Map.Entry;

import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
import se.nordicehealth.servlet.impl.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.RequestProcesser;

public class _GetClinics extends RequestProcesser {
	private PPCDatabase db;
	
	public _GetClinics(IPacketData packetData, PPCLogger logger, PPCDatabase db) {
		super(packetData, logger);
		this.db = db;
	}

	public MapData processRequest(MapData in) {
		MapData out = packetData.getMapData();
		out.put(_TYPE, AdminTypes.GET_CLINICS);

		MapData data = packetData.getMapData();
		String result = packetData.getMapData().toString();
		try {
			result = _retrieveClinics().toString();
		} catch (Exception e) { }
		data.put(AdminData.AdminGetClinics.CLINICS, result);

		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _retrieveClinics() throws Exception {
		Map<Integer, String> _clinics = db.getClinics();
		MapData clinics = packetData.getMapData();
		for (Entry<Integer, String> e : _clinics.entrySet())
			clinics.put(e.getKey(), e.getValue());
		return clinics;
	}
}