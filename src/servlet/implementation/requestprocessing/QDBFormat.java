package servlet.implementation.requestprocessing;

import java.util.ArrayList;
import java.util.List;

import common.implementation.Constants.QuestionTypes;
import servlet.core.interfaces.Database;
import servlet.core.statistics.containers.Area;
import servlet.core.statistics.containers.MultipleOption;
import servlet.core.statistics.containers.SingleOption;
import servlet.core.statistics.containers.Slider;
import servlet.core.statistics.containers.Statistics;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;

public class QDBFormat {
	Database db;
	PacketData packetData;
	public QDBFormat(Database db, PacketData packetData) {
		this.db = db;
		this.packetData = packetData;
	}
	
	public String getDBFormat(MapData fc) throws Exception {
		String val = null;
		if ((val = fc.get(QuestionTypes.SINGLE_OPTION)) != null) {
			return db.escapeReplace(String.format("option%d", Integer.parseInt(val)));
		} else if ((val = fc.get(QuestionTypes.MULTIPLE_OPTION)) != null) {
			List<String> lstr = new ArrayList<String>();
			for (String str : packetData.getListData(val).iterable()) {
				lstr.add(String.format("option%d", Integer.parseInt(str)));
			}
			return db.escapeReplace(lstr);
		} else if ((val = fc.get(QuestionTypes.SLIDER)) != null) {
			return db.escapeReplace(String.format("slider%d", Integer.parseInt(val)));
		} else if ((val = fc.get(QuestionTypes.AREA)) != null) {
			return db.escapeReplace(val);
		} else {
			return db.escapeReplace("");
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