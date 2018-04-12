package servlet.implementation.io;

import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import servlet.core._Logger;

public class PacketData implements _PacketData {
	
    public PacketData(JSONParser parser, _Logger logger) {
        this.parser = parser;
        this.logger = logger;
    }

    @Override
	public MapData getMapData() {
		return new MapData(new JSONObject());
	}

    @Override
	public MapData getMapData(String str) {
		try {
			return new MapData((JSONObject) parser.parse(str));
		} catch (org.json.simple.parser.ParseException e) {
			logger.log(Level.INFO, "Malformed JSONObject string", e);
			return getMapData();
		} catch (ClassCastException e) {
			logger.log(Level.INFO, "Malformed JSONObject string", e);
			return getMapData();
		}
	}

    @Override
	public ListData getListData() {
        return new ListData(new JSONArray());
	}

    @Override
	public ListData getListData(String str) {
		try {
	        return new ListData((JSONArray) parser.parse(str));
		} catch (org.json.simple.parser.ParseException e) {
			logger.log(Level.INFO, "Malformed JSONArray string", e);
	        return getListData();
		} catch (ClassCastException e) {
			logger.log(Level.INFO, "Malformed JSONArray string", e);
	        return getListData();
		}
	}

    private JSONParser parser;
    private _Logger logger;
}