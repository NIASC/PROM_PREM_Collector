package servlet.implementation.io;

public interface IPacketData {
	MapData getMapData();
	MapData getMapData(String str);
	ListData getListData();
	ListData getListData(String str);
}