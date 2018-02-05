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
import static common.implementation.Constants.Packet.TYPE;
import static common.implementation.Constants.Packet.DATA;

import common.implementation.Constants.Packet.Data;
import common.implementation.Constants.Packet.Types;
import common.implementation.Constants.QuestionTypes;
import servlet.core.Crypto;
import servlet.core.MailMan;
import servlet.core.PPCLogger;
import servlet.core.PasswordHandle;
import static servlet.core.ServletConst._Packet._TYPE;
import static servlet.core.ServletConst._Packet._ADMIN;
import static servlet.core.ServletConst._Packet._DATA;

import servlet.core.ServletConst._Packet._Admin;
import servlet.core.ServletConst._Packet._Data;
import servlet.core.ServletConst._Packet._Types;
import servlet.core.User;
import servlet.core.UserManager;
import servlet.core._Question;
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;


public class PPC
{
	public String handleRequest(String message, String remoteAddr, String hostAddr)
	{
		try {
			MapData obj = new MapData(message);
			_Admin admin = _Admin.NO;
			if (obj.get(_ADMIN) != null) {
				try {
					admin = Constants.getEnum(_Admin.values(), obj.get(_ADMIN));
				} catch (NumberFormatException ignores) { }
			}
			if (Constants.equal(_Admin.YES, admin) && remoteAddr.equals(hostAddr)) {
				_Types _packet = Constants.getEnum(_Types.values(), obj.get(_TYPE));
				MapData out = getAdminMethod(_packet).netfunc(obj);
				out.put(_Types.__NULL__, String.format("AdminFunction -- RemoteAddr: %s, HostAddr: %s", remoteAddr, hostAddr));
				return out.toString();
			} else {
				Types packet = Constants.getEnum(Types.values(), obj.get(TYPE));
				MapData out = getUserMethod(packet).netfunc(obj);
				out.put(Types.__NULL__, String.format("UserFunction -- RemoteAddr: %s, HostAddr: %s", remoteAddr, hostAddr));
				return out.toString();	
			}
		} catch (Exception e) {
			logger.log("Unknown request", e);
			MapData out = new MapData();
			out.put(Types.__NULL__, String.format("Error -- RemoteAddr: %s, HostAddr: %s", remoteAddr, hostAddr));
			return out.toString();
		}
	}
	
	PPC()
	{
		db = MySQL_Database.getDatabase();
		um = UserManager.getUserManager();
		crypto = new SHA_Encryption();
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
		adminMethods.put(_Types.GET_USER, this::_getUser);
		adminMethods.put(_Types.GET_CLINICS, this::_getClinics);
		adminMethods.put(_Types.ADD_USER, this::_addUser);
		adminMethods.put(_Types.ADD_CLINIC, this::_addClinic);
		adminMethods.put(_Types.RSP_REGISTR, this::_respondRegistration);
	}
	
	void terminate()
	{
		um.terminate();
	}
	
	private static PPCLogger logger;
	private static JSONParser parser;
	private Map<Enum<?>, NetworkFunction> userMethods;
	private Map<Enum<?>, NetworkFunction> adminMethods;
	private Database db;
	private UserManager um;
	private Encryption crypto;
	private QDBFormat qdbf;
	
	static {
		logger = PPCLogger.getLogger();
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
	
	private NetworkFunction getAdminMethod(_Types command)
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
		out.put(_TYPE, _Types.GET_USER);

		MapData data = new MapData();
		String result = new MapData().toString();
		try {
			result = _retrieveUser(new MapData(in.get(_DATA))).toString();
		} catch (Exception e) { }
		data.put(_Data._GetUser.USER, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _getClinics(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, _Types.GET_CLINICS);

		MapData data = new MapData();
		String result = new MapData().toString();
		try {
			result = _retrieveClinics().toString();
		} catch (Exception e) { }
		data.put(_Data._GetClinics.CLINICS, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _addUser(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, _Types.ADD_USER);

		MapData data = new MapData();
		_Data._AddUser.Response result = _Data._AddUser.Response.FAIL;
		try {
			if (_storeUser(new MapData(in.get(_DATA)))) { result = _Data._AddUser.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(_Data._AddUser.RESPONSE, result);
		
		out.put(_DATA, data.toString());
		return out;
	}
	
	private MapData _addClinic(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, _Types.ADD_CLINIC);

		MapData data = new MapData();
		_Data._AddClinic.Response result = _Data._AddClinic.Response.FAIL;
		try {
			if (_storeClinic(new MapData(in.get(_DATA)))) { result = _Data._AddClinic.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(_Data._AddClinic.RESPONSE, result);
		
		out.put(_DATA, data.toString());
		return out;
	}

	private MapData _respondRegistration(MapData in)
	{
		MapData out = new MapData();
		out.put(_TYPE, _Types.RSP_REGISTR);

		MapData data = new MapData();
		_Data._RespondRegistration.Response result = _Data._RespondRegistration.Response.FAIL;
		try {
			if (_sendRegResp(new MapData(in.get(_DATA)))) { result = _Data._RespondRegistration.Response.SUCCESS; }
		} catch (Exception e) { }
		data.put(_Data._RespondRegistration.RESPONSE, result);
		
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
		String identifier = Implementations.Encryption().encryptMessage(
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
		String newSalt = hash.getNewSalt();
		
		User user = db.getUser(name);
		Data.SetPassword.Response status = PasswordHandle.newPassError(user, oldPass, newPass1, newPass2);
		if (Constants.equal(status, Data.SetPassword.Response.SUCCESS)) {
			db.setPassword(name, user.hashWithSalt(oldPass),
					hash.hashString(newPass1, newSalt), newSalt);
		}
		return status;
	}
	
	private MapData retrieveQuestions()
			throws NullPointerException, NumberFormatException
	{
		Map<Integer, _Question> questions = db.loadQuestions();
		MapData _questions = new MapData();
		for (Entry<Integer, _Question> _e : questions.entrySet()) {
			_Question _q = _e.getValue();
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
		List<String> dlist = db.loadQResultDates(user.clinic_id);

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
		
		List<Map<Integer, String>> _results = db.loadQResults(
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
		if (!ret.user.passwordMatch(inpl.get(Data.RequestLogin.Details.PASSWORD)))
			throw new NullPointerException("invalid details");

		String hash = crypto.encryptMessage(
				Long.toHexString(System.currentTimeMillis()),
				ret.user.name, crypto.getNewSalt());
		ret.uid = Long.parseLong(hash.substring(0, 2*Long.BYTES-1), 2*Long.BYTES);
		
		ret.response = um.addUser(ret.user.name, ret.uid);
		return ret;
	}
	
	private Data.RequestLogout.Response logout(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData inpl = new MapData(Crypto.decrypt(in.get(Data.RequestLogout.DETAILS)));
		long uid = Long.parseLong(inpl.get(Data.RequestLogout.Details.UID));
		return um.delUser(um.nameForUID(uid)) ? Data.RequestLogout.Response.SUCCESS : Data.RequestLogout.Response.ERROR;
	}
	
	private MapData _retrieveUser(MapData in)
			throws NullPointerException, NumberFormatException
	{
		User _user = db.getUser(in.get(_Data._GetUser.USERNAME));
		MapData user = new MapData();
		user.put(_Data._GetUser.User.CLINIC_ID, Integer.toString(_user.clinic_id));
		user.put(_Data._GetUser.User.USERNAME, _user.name);
		user.put(_Data._GetUser.User.PASSWORD, _user.password);
		user.put(_Data._GetUser.User.EMAIL, _user.email);
		user.put(_Data._GetUser.User.SALT, _user.salt);
		user.put(_Data._GetUser.User.UPDATE_PASSWORD,
				_user.update_password ? _Data._GetUser.User.UpdatePassword.YES : _Data._GetUser.User.UpdatePassword.YES);
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
		MapData details = new MapData(in.get(_Data._AddUser.DETAILS));
		int clinic_id = Integer.parseInt(details.get(_Data._AddUser.Details.CLINIC_ID));
		String name = details.get(_Data._AddUser.Details.NAME);
		String password = details.get(_Data._AddUser.Details.PASSWORD);
		String email = details.get(_Data._AddUser.Details.EMAIL);
		String salt = details.get(_Data._AddUser.Details.SALT);
		return db.addUser(clinic_id, name, password, email, salt);
	}
	
	private boolean _storeClinic(MapData in)
			throws NullPointerException, NumberFormatException
	{
		String name = in.get(_Data._AddClinic.NAME);
		return db.addClinic(name);
	}
	
	private boolean _sendRegResp(MapData in)
			throws NullPointerException, NumberFormatException,
			org.json.simple.parser.ParseException,
			ClassCastException
	{
		MapData details = new MapData(in.get(_Data._RespondRegistration.DETAILS));
		String username = details.get(_Data._RespondRegistration.Details.USERNAME);
		String password = details.get(_Data._RespondRegistration.Details.PASSWORD);
		String email = details.get(_Data._RespondRegistration.Details.EMAIL);
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
