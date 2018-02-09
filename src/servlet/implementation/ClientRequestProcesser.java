package servlet.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import common.implementation.Constants;
import static common.implementation.Packet.TYPE;
import static common.implementation.Packet.DATA;
import static servlet.implementation.AdminPacket._ADMIN;
import static servlet.implementation.AdminPacket._DATA;
import static servlet.implementation.AdminPacket._TYPE;

import common.implementation.Constants.QuestionTypes;
import common.implementation.Packet.Data;
import common.implementation.Packet.Types;
import servlet.core.ServletLogger;
import servlet.core.UserManager;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;
import servlet.implementation.AdminPacket.Admin;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;

public class ClientRequestProcesser
{
	public String handleRequest(String message, String remoteAddr, String hostAddr)
	{
		try {
			MapData obj = new MapData(message);
			Admin admin = Admin.NO;
			if (obj.get(_ADMIN) != null) {
				try {
					admin = Constants.getEnum(Admin.values(), obj.get(_ADMIN));
				} catch (NumberFormatException ignores) { }
			}
			if (Constants.equal(Admin.YES, admin) && remoteAddr.equals(hostAddr)) {
				AdminTypes _packet = Constants.getEnum(AdminTypes.values(), obj.get(_TYPE));
				return getAdminMethod(_packet).netfunc(obj).toString();
			} else {
				Types packet = Constants.getEnum(Types.values(), obj.get(TYPE));
				return getUserMethod(packet).netfunc(obj).toString();	
			}
		} catch (Exception e) {
			logger.log("Unknown request", e);
			return new MapData().toString();
		}
	}
	
	ClientRequestProcesser()
	{
		db = MySQLDatabase.MYSQL;
		um = UserManager.MANAGER;
		crypto = SHAEncryption.SHA;
		qdbf = new QDBFormat();

		userMethods = new HashMap<Enum<?>, NetworkFunction>();
		userMethods.put(Types.PING, this::ping);
		userMethods.put(Types.VALIDATE_PID, this::validatePatientID);
		userMethods.put(Types.ADD_QANS, this::addQuestionnaireAnswers);
		userMethods.put(Types.SET_PASSWORD, this::setPassword);
		userMethods.put(Types.LOAD_Q, this::loadQuestions);
		userMethods.put(Types.LOAD_QR_DATE, this::loadQResultDates);
		userMethods.put(Types.LOAD_QR, this::loadQResults);
		userMethods.put(Types.REQ_REGISTR, this::requestRegistration);
		userMethods.put(Types.REQ_LOGIN, this::requestLogin);
		userMethods.put(Types.REQ_LOGOUT, this::requestLogout);

		adminMethods = new HashMap<Enum<?>, NetworkFunction>();
		adminMethods.put(AdminTypes.GET_USER, this::_getUser);
		adminMethods.put(AdminTypes.GET_CLINICS, this::_getClinics);
		adminMethods.put(AdminTypes.ADD_USER, this::_addUser);
		adminMethods.put(AdminTypes.ADD_CLINIC, this::_addClinic);
		adminMethods.put(AdminTypes.RSP_REGISTR, this::_respondRegistration);
	}
	
	void terminate() {
		um.terminate();
	}
	
	private static ServletLogger logger;
	private static JSONParser parser;
	private Map<Enum<?>, NetworkFunction> userMethods;
	private Map<Enum<?>, NetworkFunction> adminMethods;
	private Database db;
	private UserManager um;
	private Encryption crypto;
	private QDBFormat qdbf;
	
	static {
		logger = ServletLogger.LOGGER;
		parser = new JSONParser();
	}
	
	private static Date getDate(String date)
	{
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0L);
		}
	}
	
	private NetworkFunction getUserMethod(Types command)
	{
		return userMethods.get(command);
	}
	
	private NetworkFunction getAdminMethod(AdminTypes command)
	{
		return adminMethods.get(command);
	}
	
	private MapData ping(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.PING);

		MapData data = new MapData();
		Data.Ping.Response result = Data.Ping.Response.FAIL;
		try {
			if (processPing(new MapData(in.get(DATA)))) { result = Data.Ping.Response.SUCCESS; }
		} catch (Exception ignored) { }
		data.put(Data.Ping.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData validatePatientID(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.VALIDATE_PID);

		MapData data = new MapData();
		Data.ValidatePatientID.Response result = Data.ValidatePatientID.Response.FAIL;
		try {
			if (validatePersonalID(new MapData(in.get(DATA)))) { result = Data.ValidatePatientID.Response.SUCCESS; }
		} catch (Exception ignored) { }
		data.put(Data.ValidatePatientID.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData addQuestionnaireAnswers(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.ADD_QANS);

		MapData data = new MapData();
		Data.AddQuestionnaireAnswers.Response result = Data.AddQuestionnaireAnswers.Response.FAIL;
		try {
			if (storeQestionnaireAnswers(new MapData(in.get(DATA)))) { result = Data.AddQuestionnaireAnswers.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(Data.AddQuestionnaireAnswers.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData setPassword(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.SET_PASSWORD);

		MapData data = new MapData();
		Data.SetPassword.Response result = Data.SetPassword.Response.ERROR;
		try {
			result = storePassword(new MapData(in.get(DATA)));
		} catch (Exception e) { }
		data.put(Data.SetPassword.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData loadQuestions(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.LOAD_Q);

		MapData data = new MapData();
		String result = new MapData().toString();
		try {
			result = retrieveQuestions().toString();
		} catch (Exception e) { }
		data.put(Data.LoadQuestions.QUESTIONS, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData loadQResultDates(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.LOAD_QR_DATE);

		MapData data = new MapData();
		String result = new ListData().toString();
		try {
			result = retrieveQResultDates(new MapData(in.get(DATA))).toString();
		} catch (Exception e) { }
		data.put(Data.LoadQResultDates.DATES, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData loadQResults(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.LOAD_QR);

		MapData data = new MapData();
		String result = new ListData().toString();
		try {
			result = retrieveQResults(new MapData(in.get(DATA))).toString();
		} catch (Exception e) { }
		data.put(Data.LoadQResults.RESULTS, result);
		
		out.put(DATA, data.toString());
		return out;
	}

	private MapData requestRegistration(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.REQ_REGISTR);

		MapData data = new MapData();
		Data.RequestRegistration.Response result = Data.RequestRegistration.Response.FAIL;
		try {
			if (sendRegistration(new MapData(in.get(DATA)))) { result = Data.RequestRegistration.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(Data.RequestRegistration.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}

	private MapData requestLogin(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.REQ_LOGIN);

		MapData data = new MapData();
		Data.RequestLogin.Response response = Data.RequestLogin.Response.INVALID_DETAILS;
		Data.RequestLogin.UpdatePassword update_password = Data.RequestLogin.UpdatePassword.NO;
		String uid = Long.toString(0L);
		try {
			UserLogin ret = login(new MapData(in.get(DATA)));
			response = ret.response;
			if (ret.user.update_password) { update_password = Data.RequestLogin.UpdatePassword.YES; }
			if (Constants.equal(ret.response, Data.RequestLogin.Response.SUCCESS)) { uid = Long.toString(ret.uid); }
			logger.log("User " + ret.user + " logged in and was given UID '" + uid + "'");
		} catch (Exception e) { }
		data.put(Data.RequestLogin.RESPONSE, response);
		data.put(Data.RequestLogin.UPDATE_PASSWORD, update_password);
		data.put(Data.RequestLogin.UID, uid);
		
		out.put(DATA, data.toString());
		return out;
	}

	private MapData requestLogout(MapData in)
	{
		MapData out = new MapData();
		out.put(TYPE, Types.REQ_LOGOUT);

		MapData data = new MapData();
		Data.RequestLogout.Response result = Data.RequestLogout.Response.ERROR;
		try {
			result = logout(new MapData(in.get(DATA)));
		} catch (Exception e) { }
		data.put(Data.RequestLogout.RESPONSE, result);
		
		out.put(DATA, data.toString());
		return out;
	}
	
	private MapData _getUser(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, AdminTypes.GET_USER);

		MapData data = new MapData();
		String result = new MapData().toString();
		try {
			result = _retrieveUser(new MapData(in.get(_DATA))).toString();
		} catch (Exception e) { }
		data.put(AdminData.AdminGetUser.USER, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _getClinics(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, AdminTypes.GET_CLINICS);

		MapData data = new MapData();
		String result = new MapData().toString();
		try {
			result = _retrieveClinics().toString();
		} catch (Exception e) { }
		data.put(AdminData.AdminGetClinics.CLINICS, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _addUser(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, AdminTypes.ADD_USER);

		MapData data = new MapData();
		AdminData.AdminAddUser.Response result = AdminData.AdminAddUser.Response.FAIL;
		try {
			if (_storeUser(new MapData(in.get(_DATA)))) { result = AdminData.AdminAddUser.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminAddUser.RESPONSE, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _addClinic(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, AdminTypes.ADD_CLINIC);

		MapData data = new MapData();
		AdminData.AdminAddClinic.Response result = AdminData.AdminAddClinic.Response.FAIL;
		try {
			if (_storeClinic(new MapData(in.get(_DATA)))) { result = AdminData.AdminAddClinic.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminAddClinic.RESPONSE, result);
		
		out.put(_DATA, data.toString());
		return out;
	}

	private MapData _respondRegistration(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, AdminTypes.RSP_REGISTR);

		MapData data = new MapData();
		AdminData.AdminRespondRegistration.Response result = AdminData.AdminRespondRegistration.Response.FAIL;
		try {
			if (_sendRegResp(new MapData(in.get(_DATA)))) { result = AdminData.AdminRespondRegistration.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(AdminData.AdminRespondRegistration.RESPONSE, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	// --------------------------------
	
	private boolean processPing(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.Ping.DETAILS)));
		return um.refreshIdleTimer(Long.parseLong(inpl.get(Data.Ping.Details.UID)));
	}
	
	private boolean validatePersonalID(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.ValidatePatientID.DETAIL)));
		MapData patient = new MapData(Crypto.decrypt(in.get(Data.ValidatePatientID.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.ValidatePatientID.Details.UID));
		String personalID = patient.get(Data.ValidatePatientID.Patient.PERSONAL_ID);
		
		servlet.core.interfaces.Locale loc = Implementations.Locale();
		return db.getUser(um.nameForUID(uid)) != null
				&& loc.formatPersonalID(personalID) != null;
	}
	
	private boolean storeQestionnaireAnswers(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.DETAILS)));
		MapData patient = new MapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.PATIENT)));
		
		long uid = Long.parseLong(inpl.get(Data.AddQuestionnaireAnswers.Details.UID));
		int clinic_id = db.getUser(um.nameForUID(uid)).clinic_id;

		servlet.core.interfaces.Locale loc = Implementations.Locale();
		String forename = patient.get(Data.AddQuestionnaireAnswers.Patient.FORENAME);
		String personalID = loc.formatPersonalID(patient.get(Data.AddQuestionnaireAnswers.Patient.PERSONAL_ID));
		String surname = patient.get(Data.AddQuestionnaireAnswers.Patient.SURNAME);
		if (personalID == null)
			throw new NullPointerException("malformed patient personal id");
		String identifier = Implementations.Encryption().hashMessage(
				forename, personalID, surname);

		List<String> answers = new ArrayList<String>();
		ListData m = new ListData(in.get(Data.AddQuestionnaireAnswers.QUESTIONS));
		for (String str : m.iterable())
			answers.add(qdbf.getDBFormat(new MapData(str)));
		
		return db.addPatient(clinic_id, identifier)
				&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
	}
	
	private Data.SetPassword.Response storePassword(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.SetPassword.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.SetPassword.Details.UID));
		String name = um.nameForUID(uid);
		String oldPass = inpl.get(Data.SetPassword.Details.OLD_PASSWORD);
		String newPass1 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD1);
		String newPass2 = inpl.get(Data.SetPassword.Details.NEW_PASSWORD2);

		Encryption hash = Implementations.Encryption();
		String newSalt = hash.generateNewSalt();
		
		User user = db.getUser(name);
		Data.SetPassword.Response status = PasswordHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (Constants.equal(status, Data.SetPassword.Response.SUCCESS)) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					hash.hashMessage(newPass1, newSalt), newSalt);
		}
		return status;
	}
	
	private MapData retrieveQuestions()
			throws NullPointerException, NumberFormatException
	{
		Map<Integer, QuestionData> questions = db.loadQuestions();
		MapData _questions = new MapData();
		for (Entry<Integer, QuestionData> _e : questions.entrySet()) {
			QuestionData _q = _e.getValue();
			MapData _question = new MapData();
			ListData options = new ListData();
			for (String str : _q.options) {
				options.add(str);
			}
			_question.put(Data.LoadQuestions.Question.OPTIONS, options.toString());
			_question.put(Data.LoadQuestions.Question.TYPE, _q.type);
			_question.put(Data.LoadQuestions.Question.ID, Integer.toString(_q.id));
			_question.put(Data.LoadQuestions.Question.QUESTION, _q.question);
			_question.put(Data.LoadQuestions.Question.DESCRIPTION, _q.description);
			_question.put(Data.LoadQuestions.Question.OPTIONAL,
					_q.optional ? Data.LoadQuestions.Question.Optional.YES : Data.LoadQuestions.Question.Optional.NO);
			_question.put(Data.LoadQuestions.Question.MAX_VAL, Integer.toString(_q.max_val));
			_question.put(Data.LoadQuestions.Question.MIN_VAL, Integer.toString(_q.min_val));
			
			_questions.put(_e.getKey(), _question.toString());
		}
		return _questions;
	}
	
	private ListData retrieveQResultDates(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.LoadQResultDates.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.LoadQResultDates.Details.UID));
		User user = db.getUser(um.nameForUID(uid));
		List<String> dlist = db.loadQuestionResultDates(user.clinic_id);

		ListData dates = new ListData();
		for (String str : dlist) {
			dates.add(str);
		}
		return dates;
	}
	
	private ListData retrieveQResults(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.LoadQResults.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.LoadQResults.Details.UID));
		User _user = db.getUser(um.nameForUID(uid));
		ListData questions = new ListData(in.get(Data.LoadQResults.QUESTIONS));
		List<Integer> qlist = new ArrayList<Integer>();
		for (String str : questions.iterable()) {
			qlist.add(Integer.parseInt(str));
		}
		
		List<Map<Integer, String>> _results = db.loadQuestionResults(
				_user.clinic_id, qlist,
				getDate(in.get(Data.LoadQResults.BEGIN)),
				getDate(in.get(Data.LoadQResults.END)));

		ListData results = new ListData();
		for (Map<Integer, String> m : _results) {
			MapData answers = new MapData();
			for (Entry<Integer, String> e : m.entrySet())
				answers.put(e.getKey(), qdbf.getQFormat(e.getValue()));
			results.add(answers.toString());
		}
		return results;
	}
	
	private boolean sendRegistration(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.RequestRegistration.DETAILS)));
		String name = inpl.get(Data.RequestRegistration.Details.NAME);
		String email = inpl.get(Data.RequestRegistration.Details.EMAIL);
		String clinic = inpl.get(Data.RequestRegistration.Details.CLINIC);
		
		return MailMan.sendRegReq(name, email, clinic);
	}
	
	private UserLogin login(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		UserLogin ret = new UserLogin();
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.RequestLogin.DETAILS)));
		ret.user = db.getUser(inpl.get(Data.RequestLogin.Details.USERNAME));
		if (!ret.user.passwordMatches(inpl.get(Data.RequestLogin.Details.PASSWORD))) {
			throw new NullPointerException("invalid details");
		}

		String hash = crypto.hashMessage(
				Long.toHexString(System.currentTimeMillis()),
				ret.user.name, crypto.generateNewSalt());
		ret.uid = Long.parseLong(hash.substring(0, 2*Long.BYTES-1), 2*Long.BYTES);
		
		ret.response = um.addUserToListOfOnline(ret.user.name, ret.uid);
		return ret;
	}
	
	private Data.RequestLogout.Response logout(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.RequestLogout.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.RequestLogout.Details.UID));
		logger.log("User " + um.nameForUID(uid) + " with UID '" + uid + "' logged out");
		return um.delUserFromListOfOnline(um.nameForUID(uid)) ? Data.RequestLogout.Response.SUCCESS : Data.RequestLogout.Response.ERROR;
	}
	
	private MapData _retrieveUser(MapData in)
			throws NullPointerException, NumberFormatException
	{
		User _user = db.getUser(in.get(AdminData.AdminGetUser.USERNAME));
		MapData user = new MapData();
		user.put(AdminData.AdminGetUser.User.CLINIC_ID, Integer.toString(_user.clinic_id));
		user.put(AdminData.AdminGetUser.User.USERNAME, _user.name);
		user.put(AdminData.AdminGetUser.User.PASSWORD, _user.password);
		user.put(AdminData.AdminGetUser.User.EMAIL, _user.email);
		user.put(AdminData.AdminGetUser.User.SALT, _user.salt);
		user.put(AdminData.AdminGetUser.User.UPDATE_PASSWORD,
				_user.update_password ? AdminData.AdminGetUser.User.UpdatePassword.YES : AdminData.AdminGetUser.User.UpdatePassword.YES);
		return user;
	}
	
	private MapData _retrieveClinics()
			throws NullPointerException, NumberFormatException
	{
		Map<Integer, String> _clinics = db.getClinics();
		MapData clinics = new MapData();
		for (Entry<Integer, String> e : _clinics.entrySet())
			clinics.put(e.getKey(), e.getValue());
		return clinics;
	}
	
	private boolean _storeUser(MapData in)
			throws NullPointerException, NumberFormatException,
			IllegalArgumentException, org.json.simple.parser.ParseException
	{
		MapData details = new MapData(in.get(AdminData.AdminAddUser.DETAILS));
		int clinic_id = Integer.parseInt(details.get(AdminData.AdminAddUser.Details.CLINIC_ID));
		String name = details.get(AdminData.AdminAddUser.Details.NAME);
		String password = details.get(AdminData.AdminAddUser.Details.PASSWORD);
		String email = details.get(AdminData.AdminAddUser.Details.EMAIL);
		String salt = details.get(AdminData.AdminAddUser.Details.SALT);
		return db.addUser(clinic_id, name, password, email, salt);
	}
	
	private boolean _storeClinic(MapData in)
			throws NullPointerException, NumberFormatException
	{
		String name = in.get(AdminData.AdminAddClinic.NAME);
		return db.addClinic(name);
	}
	
	private boolean _sendRegResp(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData details = new MapData(in.get(AdminData.AdminRespondRegistration.DETAILS));
		String username = details.get(AdminData.AdminRespondRegistration.Details.USERNAME);
		String password = details.get(AdminData.AdminRespondRegistration.Details.PASSWORD);
		String email = details.get(AdminData.AdminRespondRegistration.Details.EMAIL);
		return MailMan.sendRegResp(username, password, email);
	}
	
	private static class MapData
	{
		JSONObject jobj;
		Map<String, String> jmap;
		
		MapData()
		{
			this((JSONObject) null);
		}
		
		@SuppressWarnings("unchecked")
		MapData(JSONObject jobj)
		{
			this.jobj = jobj != null ? jobj : new JSONObject();
			this.jmap = (Map<String, String>) this.jobj;
		}
		
		MapData(String jsonString)
				throws org.json.simple.parser.ParseException,
				ClassCastException
		{
			this((JSONObject) parser.parse(jsonString));
		}
		
		void put(Enum<?> key, Enum<?> value)
		{
			jmap.put(Integer.toString(key.ordinal()),
					Integer.toString(value.ordinal()));
		}
		
		void put(Enum<?> key, String value)
		{
			jmap.put(Integer.toString(key.ordinal()), value);
		}
		
		void put(Integer key, String value)
		{
			jmap.put(Integer.toString(key), value);
		}
		
		String get(Enum<?> key)
		{
			return jmap.get(Integer.toString(key.ordinal()));
		}
		
		@Override
		public String toString()
		{
			return jobj.toString();
		}
	}
	
	private static class ListData
	{
		JSONArray jarr;
		List<String> jlist;
		
		ListData()
		{
			this((JSONArray) null);
		}
		
		@SuppressWarnings("unchecked")
		ListData(JSONArray jarr)
		{
			this.jarr = jarr != null ? jarr : new JSONArray();
			this.jlist = (List<String>) this.jarr;
		}
		
		ListData(String jsonString)
				throws org.json.simple.parser.ParseException,
				ClassCastException
		{
			this((JSONArray) parser.parse(jsonString));
		}
		
		void add(String value)
		{
			jlist.add(value);
		}
		
		Iterable<String> iterable()
		{
			return Collections.unmodifiableList(jlist);
		}
		
		@Override
		public String toString()
		{
			return jarr.toString();
		}
	}

	@FunctionalInterface
	private interface NetworkFunction
	{
		public MapData netfunc(MapData in) throws Exception;
	}

	private class QDBFormat
	{
		String getDBFormat(MapData fc)
				throws org.json.simple.parser.ParseException,
				ClassCastException,
				NumberFormatException
		{
			String val = null;
			if ((val = fc.get(QuestionTypes.SINGLE_OPTION)) != null) {
				return db.escapeReplace(String.format("option%d", Integer.parseInt(val)));
			} else if ((val = fc.get(QuestionTypes.MULTIPLE_OPTION)) != null) {
				List<String> lstr = new ArrayList<>();
				for (String str : new ListData(val).iterable()) {
					lstr.add(String.format("option%d", Integer.parseInt(str)));
				}
				return db.escapeReplace(lstr);
			} else if ((val = fc.get(QuestionTypes.SLIDER)) != null) {
				return db.escapeReplace(String.format("slider%d", Integer.parseInt(val)));
			} else if ((val = fc.get(QuestionTypes.AREA)) != null) {
				return db.escapeReplace(val);
			} else {
				return db.escapeReplace("");
			}
		}
		
		String getQFormat(String dbEntry)
		{
			MapData fmt = new MapData();
			if (dbEntry == null || dbEntry.trim().isEmpty()) {
				return fmt.toString();
			}
			
			if (dbEntry.startsWith("option")) {
				fmt.put(QuestionTypes.SINGLE_OPTION, dbEntry.substring("option".length()));
			} else if (dbEntry.startsWith("slider")) {
				fmt.put(QuestionTypes.SLIDER, dbEntry.substring("slider".length()));
			} else if (db.isSQLList(dbEntry)) {
                /* multiple answers */
				List<String> entries = db.SQLListToJavaList(dbEntry);
				ListData options = new ListData();
				if (entries.get(0).startsWith("option")) {
                    /* multiple option */
					for (String str : entries)
						options.add(str.substring("option".length()));
					fmt.put(QuestionTypes.MULTIPLE_OPTION, options.toString());
				}
			} else {
                /* must be plain text entry */
				fmt.put(QuestionTypes.AREA, dbEntry);
			}
			return fmt.toString();
		}
	}
	
	private class UserLogin
	{
		User user;
		long uid;
		Data.RequestLogin.Response response;
	}
}
