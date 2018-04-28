package niasc.servlet.implementation.requestprocessing.admin;

import org.json.simple.parser.JSONParser;

import niasc.phony.database.PhonyConnection;
import niasc.phony.database.PhonyDataSource;
import niasc.phony.database.PhonyResultSet;
import niasc.phony.database.PhonyStatement;
import niasc.servlet.LoggerForTesting;
import servlet.core.PPCLogger;
import servlet.implementation.MySQLDatabase;
import servlet.implementation.io.IPacketData;
import servlet.implementation.io.PacketData;

public class RequestDatabaseFactory {
	public IPacketData pd;
	public MySQLDatabase db;
	public PPCLogger logger;
	
	public PhonyDataSource ds;
	public PhonyStatement s;
	public PhonyResultSet rs;
	
	public static RequestDatabaseFactory newInstance() {
		return new RequestDatabaseFactory();
	}

	public RequestDatabaseFactory() {
		logger = new LoggerForTesting();
		
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		ds = new PhonyDataSource(new PhonyConnection(s));
		db = new MySQLDatabase(ds, null, logger);
		
		pd = new PacketData(new JSONParser(), logger);
	}

}
