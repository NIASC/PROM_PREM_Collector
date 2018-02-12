package servlet.implementation.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public enum PacketData {
	instance;

    public MapData getMapData() {
		return new MapData(new JSONObject());
	}

    public MapData getMapData(String str) {
		try {
			return new MapData((JSONObject) parser.parse(str));
		} catch (Exception e) {
			return getMapData();
		}
	}

    public ListData getListData() {
        return new ListData(new JSONArray());
	}

    public ListData getListData(String str) {
		try {
	        return new ListData((JSONArray) parser.parse(str));
		} catch (Exception e) {
	        return getListData();
		}
	}

    private JSONParser parser;
	
    private PacketData() {
        parser = new JSONParser();
    }
}