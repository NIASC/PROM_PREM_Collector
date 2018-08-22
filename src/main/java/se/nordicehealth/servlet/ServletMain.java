package se.nordicehealth.servlet;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import se.nordicehealth.common.impl.Packet.Types;
import se.nordicehealth.res.Resources;
import se.nordicehealth.servlet.core.PPCClientRequestProcesser;
import se.nordicehealth.servlet.core.PPCDatabase;
import se.nordicehealth.servlet.core.PPCEncryption;
import se.nordicehealth.servlet.core.PPCFileHandlerUpdate;
import se.nordicehealth.servlet.core.PPCLogger;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.core.PPCUserManager;
import se.nordicehealth.servlet.core.Servlet;
import se.nordicehealth.servlet.core.usermanager.RegisteredOnlineUserManager;
import se.nordicehealth.servlet.core.usermanager.ThreadedActivityMonitor;
import se.nordicehealth.servlet.core.usermanager.UserManager;
import se.nordicehealth.servlet.impl.ClientRequestProcesser;
import se.nordicehealth.servlet.impl.Crypto;
import se.nordicehealth.servlet.impl.DiskFileHandlerUpdate;
import se.nordicehealth.servlet.impl.LocaleSE;
import se.nordicehealth.servlet.impl.MySQLDatabase;
import se.nordicehealth.servlet.impl.PasswordHandle;
import se.nordicehealth.servlet.impl.SHAEncryption;
import se.nordicehealth.servlet.impl.ServletLogger;
import se.nordicehealth.servlet.impl.AdminPacket.AdminTypes;
import se.nordicehealth.servlet.impl.io.IPacketData;
import se.nordicehealth.servlet.impl.io.PacketData;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.MailManFactory;
import se.nordicehealth.servlet.impl.mail.MessageGenerator;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationRequest;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationResponse;
import se.nordicehealth.servlet.impl.request.QDBFormat;
import se.nordicehealth.servlet.impl.request.RequestProcesser;
import se.nordicehealth.servlet.impl.request.admin._AddClinic;
import se.nordicehealth.servlet.impl.request.admin._AddUser;
import se.nordicehealth.servlet.impl.request.admin._GetClinics;
import se.nordicehealth.servlet.impl.request.admin._GetUser;
import se.nordicehealth.servlet.impl.request.admin._RespondRegistration;
import se.nordicehealth.servlet.impl.request.user.AddQuestionnaireAnswers;
import se.nordicehealth.servlet.impl.request.user.LoadQResultDates;
import se.nordicehealth.servlet.impl.request.user.LoadQResults;
import se.nordicehealth.servlet.impl.request.user.LoadQuestions;
import se.nordicehealth.servlet.impl.request.user.Ping;
import se.nordicehealth.servlet.impl.request.user.RequestLogin;
import se.nordicehealth.servlet.impl.request.user.RequestLogout;
import se.nordicehealth.servlet.impl.request.user.RequestRegistration;
import se.nordicehealth.servlet.impl.request.user.SetPassword;
import se.nordicehealth.servlet.impl.request.user.ValidatePatientID;

public class ServletMain extends HttpServlet {
	
	@Override
	public void init() throws ServletException
	{
		try {
			PPCLogger logger = loadLogger();
			PPCUserManager um = loadUserManager();
			PPCStringScramble encryption = loadStringScrambler(logger);
			PPCDatabase db = loadDatabase(logger, encryption);
			IPacketData packetData = loadPacketData(logger);
			Map<Types, RequestProcesser> userMethods = loadUserResponseHandling(logger, um, encryption, db, packetData);
			Map<AdminTypes, RequestProcesser> adminMethods = loadAdminResponseHandling(logger, db, packetData);
			PPCClientRequestProcesser requestProcesser = new ClientRequestProcesser(logger, packetData, um, userMethods, adminMethods);
			servlet = new Servlet(loadMainPage(), requestProcesser, logger);
		} catch (Exception e) {
			throw new ServletException(e.getMessage());
		}
	}

	private Map<Types, RequestProcesser> loadUserResponseHandling(PPCLogger logger, PPCUserManager um, PPCStringScramble encryption, PPCDatabase db, IPacketData packetData) throws IOException
	{
		QDBFormat qdbf = loadDatabaseJavaFormatConverter(db, packetData);
		se.nordicehealth.servlet.core.PPCLocale locale = loadLocaleFormats();
		PPCEncryption crypto = loadEnctryption();
		PasswordHandle passHandle = new PasswordHandle();
		MailMan umm = MailManFactory.newUserInstance(Resources.EMAIL_ACCOUNTS_CONFIG, Resources.EMAIL_CONFIG, logger);
		RegistrationRequest req = loadRequestTemplate(Resources.REGREQ_EMAIL_BODY_TEMPLATE);
		
		Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
		userMethods.put(Types.PING, new Ping(packetData, logger, um, crypto));
		userMethods.put(Types.VALIDATE_PID, new ValidatePatientID(um, db, packetData, logger, crypto, locale));
		userMethods.put(Types.ADD_QANS, new AddQuestionnaireAnswers(packetData, logger, um, db, qdbf, encryption, locale, crypto));
		userMethods.put(Types.SET_PASSWORD, new SetPassword(um, db, packetData, logger, encryption, crypto, passHandle));
		userMethods.put(Types.LOAD_Q, new LoadQuestions(packetData, logger, db));
		userMethods.put(Types.LOAD_QR_DATE, new LoadQResultDates(packetData, logger, um, db, crypto));
		userMethods.put(Types.LOAD_QR, new LoadQResults(packetData, logger, um, db, qdbf, crypto));
		userMethods.put(Types.REQ_REGISTR, new RequestRegistration(db, packetData, logger, umm, req, crypto));
		userMethods.put(Types.REQ_LOGIN, new RequestLogin(packetData, logger, um, db, encryption, crypto));
		userMethods.put(Types.REQ_LOGOUT, new RequestLogout(um, db, packetData, logger, crypto));
		return userMethods;
	}

	private Map<AdminTypes, RequestProcesser> loadAdminResponseHandling(PPCLogger logger, PPCDatabase db, IPacketData packetData) throws IOException
	{
		MailMan amm = MailManFactory.newAdminInstance(Resources.EMAIL_ACCOUNTS_CONFIG, Resources.EMAIL_CONFIG, logger);
		RegistrationResponse resp = loadResponseTemplate(Resources.REGRESP_EMAIL_BODY_TEMPLATE);
		
		Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
		adminMethods.put(AdminTypes.GET_USER, new _GetUser(packetData, logger, db));
		adminMethods.put(AdminTypes.GET_CLINICS, new _GetClinics(packetData, logger, db));
		adminMethods.put(AdminTypes.ADD_USER, new _AddUser(packetData, logger, db));
		adminMethods.put(AdminTypes.ADD_CLINIC, new _AddClinic(packetData, logger, db));
		adminMethods.put(AdminTypes.RSP_REGISTR, new _RespondRegistration(packetData, logger, amm, resp));
		return adminMethods;
	}

	private PPCEncryption loadEnctryption() throws IOException
	{
		Properties props = new Properties();
		props.load(Resources.getStream(Resources.KEY_CONFIG));
		BigInteger mod = new BigInteger(props.getProperty("mod"), 16);
		BigInteger powPrivate = new BigInteger(props.getProperty("exp"), 16);
		BigInteger powPublic = new BigInteger(props.getProperty("public"), 16);
		props.clear();
		return new Crypto(powPrivate, mod, powPublic);
	}

	private se.nordicehealth.servlet.core.PPCLocale loadLocaleFormats()
	{
		return new LocaleSE();
	}

	private QDBFormat loadDatabaseJavaFormatConverter(PPCDatabase db, IPacketData packetData)
	{
		return new QDBFormat(db, packetData);
	}

	private IPacketData loadPacketData(PPCLogger logger)
	{
		return new PacketData(new JSONParser(), logger);
	}

	private PPCDatabase loadDatabase(PPCLogger logger, PPCStringScramble encryption) throws NamingException
	{
		Context initContext = new InitialContext();
		Context envContext = (Context) initContext.lookup("java:comp/env");
		DataSource dataSource = (DataSource) envContext.lookup("jdbc/prom_prem_db");
		return new MySQLDatabase(dataSource, encryption, logger);
	}

	private PPCStringScramble loadStringScrambler(PPCLogger logger) throws NoSuchAlgorithmException
	{
		return new SHAEncryption(SecureRandom.getInstance("SHA1PRNG"), MessageDigest.getInstance("SHA-256"));
	}

	private PPCUserManager loadUserManager() {
		RegisteredOnlineUserManager usr = new RegisteredOnlineUserManager();
		return new UserManager(usr, new ThreadedActivityMonitor(usr));
	}
	
	private PPCLogger loadLogger() throws IOException
	{
		Properties props = new Properties();
		props.load(Resources.getStream(Resources.SETTINGS_CONFIG));
		int logsize = Integer.parseInt(props.getProperty("logsize"));
		int logcount = Integer.parseInt(props.getProperty("logcount"));
		File logdir = new File(props.getProperty("logdir"));
		if (logdir.exists() && !logdir.isDirectory() || !logdir.exists() && !logdir.mkdirs()) {
			throw new IOException("Directory structure for logging (" + logdir.getPath() + ") could not be created.");
		}

		PPCFileHandlerUpdate handler = new DiskFileHandlerUpdate(logdir.getAbsolutePath(), logsize, logcount, "Servlet");
		Logger lgr = Logger.getLogger(ServletMain.class.getName());
		lgr.setLevel(Level.FINEST);
		return new ServletLogger(handler, lgr);
	}
	
	private RegistrationRequest loadRequestTemplate(String file) throws IOException {
		return new RegistrationRequest(new MessageGenerator(Util.loadFile(file)));
	}
	
	private RegistrationResponse loadResponseTemplate(String file) throws IOException {
		return new RegistrationResponse(new MessageGenerator(Util.loadFile(file)));
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
	
	private String loadMainPage() {
		try {
			return Util.loadFile(se.nordicehealth.res.Resources.MAIN_PAGE);
		} catch (IOException e) {
			return "<html><head>404 - Page not found</head><body>The requested page was not found.</body></html>";
		}
	}

	private static final long serialVersionUID = -2340346250534805168L;
	private Servlet servlet;
}
