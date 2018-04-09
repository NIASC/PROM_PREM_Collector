package servlet.core.usermanager;

import java.util.Collection;

public interface ConnectionManager {
	void registerConnection(ConnectionData connection);
	void deregisterConnection(ConnectionData connection);
	void deregisterConnection(long uid);
	void deregisterAllConnections();

	Collection<ConnectionData> iterable();
	int registeredConnections();
	boolean isConnected(String identifier);
	boolean isConnected(long uid);
	ConnectionData getConnection(String identifier);
	ConnectionData getConnection(long uid);
	
	String identifierForUID(long uid);
	boolean refreshInactivityTimer(long uid);
	boolean refreshIdleTimer(long uid);
}
