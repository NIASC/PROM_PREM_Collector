package se.nordicehealth.servlet.impl.request.user;

import org.json.simple.parser.JSONParser;

import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.impl.LocaleSE;
import se.nordicehealth.servlet.impl.MySQLDatabase;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.PacketData;
import se.nordicehealth.servlet.impl.request.QDBFormat;
import se.nordicehealth.zzphony.PhonyEncryption;
import se.nordicehealth.zzphony.PhonyUserManager;
import se.nordicehealth.zzphony.database.PhonyConnection;
import se.nordicehealth.zzphony.database.PhonyDataSource;
import se.nordicehealth.zzphony.database.PhonyResultSet;
import se.nordicehealth.zzphony.database.PhonyStatement;
import se.nordicehealth.zzphony.encryption.PhonyCrypto;

public class ReqProcUtil {
	public IPacketData pd;
	public MySQLDatabase db;
	public PPCLogger logger;
	
	public PhonyDataSource ds;
	public PhonyStatement s;
	public PhonyResultSet rs;
	
	public PhonyUserManager um;
	public QDBFormat qdbf;
	public PPCStringScramble encryption;
	public se.nordicehealth.servlet.core.PPCLocale locale;
	public PhonyCrypto crypto;
	
	public static ReqProcUtil newInstance() {
		return new ReqProcUtil();
	}

	public ReqProcUtil() {
		logger = new LoggerForTesting();
		encryption = new PhonyEncryption();
		
		rs = new PhonyResultSet();
		s = new PhonyStatement(rs);
		ds = new PhonyDataSource(new PhonyConnection(s));
		db = new MySQLDatabase(ds, encryption, logger);
		
		pd = new PacketData(new JSONParser(), logger);

		um = new PhonyUserManager();
		
		qdbf = new QDBFormat(db, pd);
		locale = new LocaleSE();
		
		crypto = new PhonyCrypto();
	}

}
