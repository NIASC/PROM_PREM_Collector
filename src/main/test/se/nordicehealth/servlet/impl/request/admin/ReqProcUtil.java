package se.nordicehealth.servlet.impl.request.admin;

import org.json.simple.parser.JSONParser;

import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.impl.MySQLDatabase;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.PacketData;
import se.nordicehealth.zzphony.database.PhonyConnection;
import se.nordicehealth.zzphony.database.PhonyDataSource;
import se.nordicehealth.zzphony.database.PhonyResultSet;
import se.nordicehealth.zzphony.database.PhonyStatement;

public class ReqProcUtil {
	public IPacketData pd;
	public MySQLDatabase db;
	public PPCLogger logger;
	
	public PhonyDataSource ds;
	public PhonyStatement s;
	public PhonyResultSet rs;
	
	public static ReqProcUtil newInstance() {
		return new ReqProcUtil();
	}

	public ReqProcUtil() {
		logger = new LoggerForTesting();
		
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		ds = new PhonyDataSource(new PhonyConnection(s));
		db = new MySQLDatabase(ds, null, logger);
		
		pd = new PacketData(new JSONParser(), logger);
	}

}
