package servlet.implementation.io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class PacketData implements _PacketData {
	
    public PacketData(JSONParser parser) {
        parser = new JSONParser();
    }

    @Override
	public MapData getMapData() {
		return new MapData(new JSONObject());
	}

    @Override
	public MapData getMapData(String str) {
		try {
			return new MapData((JSONObject) parser.parse(str));
		} catch (Exception e) {
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
		} catch (Exception e) {
	        return getListData();
		}
	}

    private JSONParser parser;
}