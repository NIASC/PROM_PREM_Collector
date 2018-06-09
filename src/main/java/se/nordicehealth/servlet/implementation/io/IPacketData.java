package se.nordicehealth.servlet.implementation.io;

public interface IPacketData {
	MapData getMapData();
	MapData getMapData(String str);
	ListData getListData();
	ListData getListData(String str);
	boolean isMapData(String str);
	boolean isListData(String str);
}