package servlet.implementation;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;

import common.Util;
import common.implementation.Packet.Types;
import res.Resources;
import servlet.core.ServletLogger;
import servlet.core._Logger;
import servlet.core.interfaces.Database;
import servlet.core.usermanager.RegisteredOnlineUserManager;
import servlet.core.usermanager.ThreadedActivityMonitor;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.PacketData;
import servlet.implementation.io._PacketData;
import servlet.implementation.mail.MailMan;
import servlet.implementation.mail.MailManFactory;
import servlet.implementation.mail.MessageGenerator;
import servlet.implementation.mail.emails.RegistrationRequest;
import servlet.implementation.mail.emails.RegistrationResponse;
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

public class ServletBridge extends HttpServlet {
	@Override
	public void init() throws ServletException {
		String message = "<html><head>404 - Page not found</head><body>The requested page was not found.</body></html>";
		try {
			message = Util.fileToString(res.Resources.MAIN_PAGE, "UTF-8");
		} catch (IOException e) { } catch (UnsupportedCharsetException e) { }
		
		_Logger logger = ServletLogger.LOGGER;
		_PacketData packetData = new PacketData(new JSONParser(), logger);
		RegisteredOnlineUserManager usr = new RegisteredOnlineUserManager();
		UserManager um = new UserManager(usr, new ThreadedActivityMonitor(usr));
		MailMan mm = null;
		MailMan amm = null;
		try {
			mm = MailManFactory.newInstance(Resources.EMAIL_ACCOUNTS, Resources.EMAIL_CONFIG, logger);
			amm = MailManFactory.newAdminInstance(Resources.EMAIL_ACCOUNTS, Resources.EMAIL_CONFIG, logger);
		} catch (IOException e) {
			logger.fatalLogAndAction("Email configuration could not be loaded.");
		}
		RegistrationRequest req = loadRegReqEmail(Resources.REGREQ_EMAIL_BODY);
		RegistrationResponse resp = loadRegRespEmail(Resources.REGRESP_EMAIL_BODY);
		Database db = new MySQLDatabase(logger);
		QDBFormat qdbf = new QDBFormat(db, packetData);
		Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
		Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
		userMethods.put(Types.PING, new Ping(um, db, packetData, qdbf, logger));
		userMethods.put(Types.VALIDATE_PID, new ValidatePatientID(um, db, packetData, qdbf, logger));
		userMethods.put(Types.ADD_QANS, new AddQuestionnaireAnswers(um, db, packetData, qdbf, logger));
		userMethods.put(Types.SET_PASSWORD, new SetPassword(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_Q, new LoadQuestions(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_QR_DATE, new LoadQResultDates(um, db, packetData, qdbf, logger));
		userMethods.put(Types.LOAD_QR, new LoadQResults(um, db, packetData, qdbf, logger));
		userMethods.put(Types.REQ_REGISTR, new RequestRegistration(um, db, packetData, qdbf, logger, amm, req));
		userMethods.put(Types.REQ_LOGIN, new RequestLogin(um, db, packetData, qdbf, logger));
		userMethods.put(Types.REQ_LOGOUT, new RequestLogout(um, db, packetData, qdbf, logger));
		
		adminMethods.put(AdminTypes.GET_USER, new _GetUser(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.GET_CLINICS, new _GetClinics(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.ADD_USER, new _AddUser(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.ADD_CLINIC, new _AddClinic(um, db, packetData, qdbf, logger));
		adminMethods.put(AdminTypes.RSP_REGISTR, new _RespondRegistration(um, db, packetData, qdbf, logger, mm, resp));
		
		
		servlet = new Servlet(message, new ClientRequestProcesser(logger, packetData, um, userMethods, adminMethods), logger);
	}

	@Override
	public void destroy() {
		servlet.terminate();
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		servlet.presentMainPage(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		servlet.handleRequest(req, resp);
	}

	private RegistrationRequest loadRegReqEmail(String filename) {
		String regReqBodyTemplate = "";
		try {
			regReqBodyTemplate = Util.fileToString(filename, "UTF-8");
		} catch (IOException e) { }
		return new RegistrationRequest(new MessageGenerator(regReqBodyTemplate));
	}

	private RegistrationResponse loadRegRespEmail(String filename) {
		String regRspBodyTemplate = "";
		try {
			regRspBodyTemplate = Util.fileToString(filename, "UTF-8");
		} catch (IOException e) { }
		return new RegistrationResponse(new MessageGenerator(regRspBodyTemplate));
	}

	private static final long serialVersionUID = -2340346250534805168L;
	private Servlet servlet;
}
