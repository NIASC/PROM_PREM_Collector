package se.nordicehealth.servlet.impl.request;

import java.util.ArrayList;
import java.util.List;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.stats.containers.Area;
import se.nordicehealth.servlet.core.stats.containers.MultipleOption;
import se.nordicehealth.servlet.core.stats.containers.SingleOption;
import se.nordicehealth.servlet.core.stats.containers.Slider;
import se.nordicehealth.servlet.core.stats.containers.Statistics;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.MapData;

public class QDBFormat {
	PPCDatabase db;
	IPacketData packetData;
	public QDBFormat(PPCDatabase db, IPacketData packetData) {
		this.db = db;
		this.packetData = packetData;
	}
	
	public String getDBFormat(MapData fc) throws Exception {
		String val = null;
		if ((val = fc.get(Constants.SINGLE_OPTION)) != null) {
			return String.format("option%d", Integer.parseInt(val));
		} else if ((val = fc.get(Constants.MULTIPLE_OPTION)) != null) {
			List<String> lstr = new ArrayList<String>();
			if (!packetData.isListData(val)) {
				throw new Exception("MultipleOption data is not a ListData object.");
			}
			for (String str : packetData.getListData(val).iterable()) {
				lstr.add(String.format("option%d", Integer.parseInt(str)));
			}
			return db.convertToSQLList(lstr);
		} else if ((val = fc.get(Constants.SLIDER)) != null) {
			return String.format("slider%d", Integer.parseInt(val));
		} else if ((val = fc.get(Constants.AREA)) != null) {
			return val;
		} else {
			return "";
		}
	}
	
	public Statistics getQFormat(int questionID, String dbEntry)
	{
		if (dbEntry.startsWith("option")) {
			return new SingleOption(questionID, Integer.valueOf(dbEntry.substring("option".length())));
		} else if (dbEntry.startsWith("slider")) {
			return new Slider(questionID, Integer.valueOf(dbEntry.substring("slider".length())));
		} else if (db.isSQLList(dbEntry)) {
			List<String> entries = db.SQLListToJavaList(dbEntry);
            List<Integer> lint = new ArrayList<Integer>();
			if (entries.get(0).startsWith("option")) {
				for (String str : entries)
                    lint.add(Integer.valueOf(str.substring("option".length())));
                return new MultipleOption(questionID, lint);
			}
		} else {
			return new Area(questionID, dbEntry);
		}
		return null;
	}
}