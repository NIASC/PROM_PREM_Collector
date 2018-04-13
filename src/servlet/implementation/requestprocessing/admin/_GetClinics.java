package servlet.implementation.requestprocessing.admin;

import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import java.util.Map;
import java.util.Map.Entry;

import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io._PacketData;
import servlet.implementation.requestprocessing.RequestProcesser;

public class _GetClinics extends RequestProcesser {
	private Database db;
	
	public _GetClinics(_PacketData packetData, _Logger logger, Database db) {
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