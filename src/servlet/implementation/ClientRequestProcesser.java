package servlet.implementation;

import java.util.HashMap;
import java.util.Map;

import common.implementation.Constants;
import static common.implementation.Packet.TYPE;
import static servlet.implementation.AdminPacket._ADMIN;
import static servlet.implementation.AdminPacket._TYPE;

import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.ThreadedActivityMonitor;
import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.Admin;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;
import servlet.implementation.requestprocessing.QDBFormat;
import servlet.implementation.requestprocessing.RequestProcesser;
import servlet.implementation.requestprocessing.admin._AddClinic;
import servlet.implementation.requestprocessing.admin._AddUser;
import servlet.implementation.requestprocessing.admin._GetClinics;
import servlet.implementation.requestprocessing.admin._GetUser;
import servlet.implementation.requestprocessing.admin._RespondRegistration;
import servlet.implementation.requestprocessing.user.AddQuestionnaireAnswers;
import servlet.implementation.requestprocessing.user.LoadQResultDates;
import servlet.implementation.requestprocessing.user.LoadQResults;
import servlet.implementation.requestprocessing.user.LoadQuestions;
import servlet.implementation.requestprocessing.user.Ping;
import servlet.implementation.requestprocessing.user.RequestLogin;
import servlet.implementation.requestprocessing.user.RequestLogout;
import servlet.implementation.requestprocessing.user.RequestRegistration;
import servlet.implementation.requestprocessing.user.SetPassword;
import servlet.implementation.requestprocessing.user.ValidatePatientID;

public class ClientRequestProcesser
{
	public String handleRequest(String message, String remoteAddr, String hostAddr) {
		try {
			MapData obj = packetData.getMapData(message);
			Admin admin = Admin.NO;
			if (obj.get(_ADMIN) != null) {
				try {
					admin = Constants.getEnum(Admin.values(), obj.get(_ADMIN));
				} catch (NumberFormatException ignored) { }
			}
			RequestProcesser rp = (Constants.equal(Admin.YES, admin) && remoteAddr.equals(hostAddr)) ? adminMethods.get(Constants.getEnum(AdminTypes.values(), obj.get(_TYPE))) : userMethods.get(Constants.getEnum(Types.values(), obj.get(TYPE)));
			return rp.processRequest(obj).toString();
		} catch (Exception e) {
			logger.log("Unknown request", e);
			return packetData.getMapData().toString();
		}
	}
	
	public ClientRequestProcesser() {
		logger = ServletLogger.LOGGER;
		packetData = PacketData.instance;

		RegisteredOnlineUserManager usr = new RegisteredOnlineUserManager();
		um = new UserManager(usr, new ThreadedActivityMonitor(usr));
		um.startManagement();
		
		Database db = MySQLDatabase.instance;
		QDBFormat qdbf = new QDBFormat(db, packetData);
		userMethods.put(Types.PING, new Ping(um, db, packetData, qdbf, logger));
		userMethods.put(Types.VALIDATE_PID, new ValidatePatientID(um, db, packetData, qdbf, logger));
		userMethods.put(Types.ADD_QANS, new AddQuestionnaireAnswers(um, db, packetData, qdbf, logger));
		userMethods.put(Types.SET_PASSWORD, new SetPassword(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_Q, new LoadQuestions(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_QR_DATE, new LoadQResultDates(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_QR, new LoadQResults(um, db, packetData, qdbf, logger));
		userMethods.put(Types.REQ_REGISTR, new RequestRegistration(um, db, packetData, qdbf, logger));
		userMethods.put(Types.REQ_LOGIN, new RequestLogin(um, db, packetData, qdbf, logger));
		userMethods.put(Types.REQ_LOGOUT, new RequestLogout(um, db, packetData, qdbf, logger));

		adminMethods.put(AdminTypes.GET_USER, new _GetUser(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.GET_CLINICS, new _GetClinics(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.ADD_USER, new _AddUser(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.ADD_CLINIC, new _AddClinic(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.RSP_REGISTR, new _RespondRegistration(um, db, packetData, qdbf, logger));
	}
	
	public void terminate() {
		um.stopManagement();
	}
	
	private ServletLogger logger;
	private PacketData packetData;
	private UserManager um;
	private Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
	private Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
}
