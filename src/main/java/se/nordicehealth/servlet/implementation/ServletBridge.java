package se.nordicehealth.servlet.implementation;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.json.simple.parser.JSONParser;

import se.nordicehealth.common.Util;
import se.nordicehealth.common.implementation.Packet.Types;
import se.nordicehealth.res.Resources;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.core.ServletLogger;
import se.nordicehealth.servlet.core.usermanager.RegisteredOnlineUserManager;
import se.nordicehealth.servlet.core.usermanager.ThreadedActivityMonitor;
import se.nordicehealth.servlet.core.usermanager.UserManager;
import se.nordicehealth.servlet.implementation.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.implementation.io.IPacketData;
import se.nordicehealth.servlet.implementation.io.PacketData;
import se.nordicehealth.servlet.implementation.mail.MailMan;
import se.nordicehealth.servlet.implementation.mail.MailManFactory;
import se.nordicehealth.servlet.implementation.mail.MessageGenerator;
import se.nordicehealth.servlet.implementation.mail.emails.RegistrationRequest;
import se.nordicehealth.servlet.implementation.mail.emails.RegistrationResponse;
import se.nordicehealth.servlet.implementation.requestprocessing.QDBFormat;
import se.nordicehealth.servlet.implementation.requestprocessing.RequestProcesser;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._AddClinic;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._AddUser;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._GetClinics;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._GetUser;
import se.nordicehealth.servlet.implementation.requestprocessing.admin._RespondRegistration;
import se.nordicehealth.servlet.implementation.requestprocessing.user.AddQuestionnaireAnswers;
import se.nordicehealth.servlet.implementation.requestprocessing.user.LoadQResultDates;
import se.nordicehealth.servlet.implementation.requestprocessing.user.LoadQResults;
import se.nordicehealth.servlet.implementation.requestprocessing.user.LoadQuestions;
import se.nordicehealth.servlet.implementation.requestprocessing.user.Ping;
import se.nordicehealth.servlet.implementation.requestprocessing.user.RequestLogin;
import se.nordicehealth.servlet.implementation.requestprocessing.user.RequestLogout;
import se.nordicehealth.servlet.implementation.requestprocessing.user.RequestRegistration;
import se.nordicehealth.servlet.implementation.requestprocessing.user.SetPassword;
import se.nordicehealth.servlet.implementation.requestprocessing.user.ValidatePatientID;

public class ServletBridge extends HttpServlet {
	@Override
	public void init() throws ServletException {
		String message = "<html><head>404 - Page not found</head><body>The requested page was not found.</body></html>";
		try {
			message = Util.fileToString(se.nordicehealth.res.Resources.MAIN_PAGE, "UTF-8");
		} catch (IOException e) { } catch (UnsupportedCharsetException e) { }
		
		PPCLogger logger = ServletLogger.LOGGER;
		IPacketData packetData = new PacketData(new JSONParser(), logger);
		RegisteredOnlineUserManager usr = new RegisteredOnlineUserManager();
		PPCUserManager um = new UserManager(usr, new ThreadedActivityMonitor(usr));
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

		DataSource dataSource = null;
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:comp/env");
			dataSource = (DataSource) envContext.lookup("jdbc/prom_prem_db");
		} catch (NamingException e) {
			logger.log("FATAL: Could not load database configuration", e);
			System.exit(1);
		}

		SecureRandom sr = null;
		MessageDigest md = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			logger.log(String.format("FATAL: Hashing algorithms %s and/or %s is not available.", "SHA1PRNG", "SHA-256"));
			System.exit(1);
		}
		PPCStringScramble encryption = new SHAEncryption(sr, md);
		PPCDatabase db = new MySQLDatabase(dataSource, encryption, logger);
		
		QDBFormat qdbf = new QDBFormat(db, packetData);
		se.nordicehealth.servlet.core.PPCLocale locale = new LocaleSE();

		BigInteger powPrivate = null, mod = null, powPublic = null;
		try {
			Properties props = new Properties();
			props.load(Resources.getStream(Resources.KEY_PATH));
			mod = new BigInteger(props.getProperty("mod"), 16);
			powPrivate = new BigInteger(props.getProperty("exp"), 16);
			powPrivate = new BigInteger(props.getProperty("public"), 16);
			props.clear();
		} catch (IOException _e) { } catch (IllegalArgumentException _e) { }
		Crypto crypto = new Crypto(powPrivate, mod, powPublic);
		
		PasswordHandle passHandle = new PasswordHandle();
		Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
		Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
		userMethods.put(Types.PING, new Ping(packetData, logger, um, crypto));
		userMethods.put(Types.VALIDATE_PID, new ValidatePatientID(um, db, packetData, logger, crypto, locale));
		userMethods.put(Types.ADD_QANS, new AddQuestionnaireAnswers(packetData, logger, um, db, qdbf, encryption, locale, crypto));
		userMethods.put(Types.SET_PASSWORD, new SetPassword(um, db, packetData, logger, encryption, crypto, passHandle));
		userMethods.put(Types.LOAD_Q, new LoadQuestions(packetData, logger, db));
		userMethods.put(Types.LOAD_QR_DATE, new LoadQResultDates(packetData, logger, um, db, crypto));
		userMethods.put(Types.LOAD_QR, new LoadQResults(packetData, logger, um, db, qdbf, crypto));
		userMethods.put(Types.REQ_REGISTR, new RequestRegistration(db, packetData, logger, amm, req, crypto));
		userMethods.put(Types.REQ_LOGIN, new RequestLogin(packetData, logger, um, db, encryption, crypto));
		userMethods.put(Types.REQ_LOGOUT, new RequestLogout(um, db, packetData, logger, crypto));
		
		adminMethods.put(AdminTypes.GET_USER, new _GetUser(packetData, logger, db));
		adminMethods.put(AdminTypes.GET_CLINICS, new _GetClinics(packetData, logger, db));
		adminMethods.put(AdminTypes.ADD_USER, new _AddUser(packetData, logger, db));
		adminMethods.put(AdminTypes.ADD_CLINIC, new _AddClinic(packetData, logger, db));
		adminMethods.put(AdminTypes.RSP_REGISTR, new _RespondRegistration(packetData, logger, mm, resp));
		
		
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
