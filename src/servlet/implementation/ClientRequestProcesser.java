package servlet.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import servlet.core.interfaces.Database;
import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;
import servlet.core.statistics.Question;
import servlet.core.statistics.StatisticsContainer;
import servlet.core.statistics.StatisticsData;
import servlet.core.statistics.containers.Area;
import servlet.core.statistics.containers.MultipleOption;
import servlet.core.statistics.containers.SingleOption;
import servlet.core.statistics.containers.Slider;
import servlet.core.statistics.containers.Statistics;
import servlet.core.usermanager.UserManager;
import servlet.implementation.AdminPacket.Admin;
import servlet.implementation.AdminPacket.AdminData;
import servlet.implementation.AdminPacket.AdminTypes;
import servlet.implementation.io.ListData;
import servlet.implementation.io.MapData;
import servlet.implementation.io.PacketData;

public class ClientRequestProcesser
{
	public String handleRequest(String message, String remoteAddr, String hostAddr)
	{
		try {
			MapData obj = packetData.getMapData(message);
			Admin admin = Admin.NO;
			if (obj.get(_ADMIN) != null) {
				try {
					admin = Constants.getEnum(Admin.values(), obj.get(_ADMIN));
				} catch (NumberFormatException ignores) { }
			}
			RequestProcesser rp = null;
			if (Constants.equal(Admin.YES, admin) && remoteAddr.equals(hostAddr)) {
				rp = adminMethods.get(Constants.getEnum(AdminTypes.values(), obj.get(_TYPE)));
			} else {
				rp = userMethods.get(Constants.getEnum(Types.values(), obj.get(TYPE)));	
			}
			return rp.processRequest(obj).toString();
		} catch (Exception e) {
			logger.log("Unknown request", e);
			return packetData.getMapData().toString();
		}
	}
	
	public ClientRequestProcesser() {
		userMethods.put(Types.PING, Ping.instance);
		userMethods.put(Types.VALIDATE_PID, ValidatePatientID.instance);
		userMethods.put(Types.ADD_QANS, AddQuestionnaireAnswers.instance);
		userMethods.put(Types.SET_PASSWORD, SetPassword.instance);
		userMethods.put(Types.LOAD_Q, LoadQuestions.instance);
		userMethods.put(Types.LOAD_QR_DATE, LoadQResultDates.instance);
		userMethods.put(Types.LOAD_QR, LoadQResults.instance);
		userMethods.put(Types.REQ_REGISTR, RequestRegistration.instance);
		userMethods.put(Types.REQ_LOGIN, RequestLogin.instance);
		userMethods.put(Types.REQ_LOGOUT, RequestLogout.instance);

		adminMethods.put(AdminTypes.GET_USER, _GetUser.instance);
		adminMethods.put(AdminTypes.GET_CLINICS, _GetClinics.instance);
		adminMethods.put(AdminTypes.ADD_USER, _AddUser.instance);
		adminMethods.put(AdminTypes.ADD_CLINIC, _AddClinic.instance);
		adminMethods.put(AdminTypes.RSP_REGISTR, _RespondRegistration.instance);
	}
	
	public void terminate() {
		UserManager.instance.terminate();
	}
	
	private static final ServletLogger logger = ServletLogger.LOGGER;
	private static PacketData packetData = PacketData.instance;
	private Map<Types, RequestProcesser> userMethods = new HashMap<Types, RequestProcesser>();
	private Map<AdminTypes, RequestProcesser> adminMethods = new HashMap<AdminTypes, RequestProcesser>();
	
	private static Date getDate(String date) {
		try {
			return (new SimpleDateFormat("yyyy-MM-dd")).parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0L);
		}
	}
	
	private interface RequestProcesser {
		UserManager um = UserManager.instance;
		Database db = MySQLDatabase.instance;
		PacketData packetData = PacketData.instance;
		QDBFormat qdbf = QDBFormat.instance;
		MapData processRequest(MapData in) throws Exception;
	}
	
	private static abstract class LoggedInMethods {
		public abstract boolean refreshTimer(long uid);
	}
	
	private interface LoggedInRequestProcesser extends RequestProcesser {
		DefaultLoggedInRequestProcesser defimpl = new DefaultLoggedInRequestProcesser();
		
		class DefaultLoggedInRequestProcesser extends ClientRequestProcesser.LoggedInMethods {
			@Override
			public boolean refreshTimer(long uid) {
				return um.refreshInactivityTimer(uid);
			}
		}
	}
	
	private interface IdleRequestProcesser extends RequestProcesser {
		DefaultLoggedInRequestProcesser defimpl = new DefaultLoggedInRequestProcesser();
		
		class DefaultLoggedInRequestProcesser extends ClientRequestProcesser.LoggedInMethods {
			@Override
			public boolean refreshTimer(long uid) {
				return um.refreshInactivityTimer(uid);
			}
		}
	}
	
	private enum Ping implements IdleRequestProcesser {
		instance;
		
		@Override
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.PING);

			MapData data = packetData.getMapData();
			Data.Ping.Response result = Data.Ping.Response.FAIL;
			try {
				result = processPing(packetData.getMapData(in.get(DATA)));
			} catch (Exception ignored) { }
			data.put(Data.Ping.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private Data.Ping.Response processPing(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.Ping.DETAILS)));
			long uid = Long.parseLong(inpl.get(Data.Ping.Details.UID));
			if (um.isOnline(uid)) {
				return defimpl.refreshTimer(uid) ? Data.Ping.Response.SUCCESS : Data.Ping.Response.FAIL;
			}
			return Data.Ping.Response.NOT_ONLINE;
		}
	}
	
	private enum ValidatePatientID implements LoggedInRequestProcesser {
		instance;

		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.VALIDATE_PID);

			MapData data = packetData.getMapData();
			Data.ValidatePatientID.Response result = Data.ValidatePatientID.Response.FAIL;
			try {
				if (validatePersonalID(packetData.getMapData(in.get(DATA)))) { result = Data.ValidatePatientID.Response.SUCCESS; }
			} catch (Exception ignored) { }
			data.put(Data.ValidatePatientID.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private boolean validatePersonalID(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.ValidatePatientID.DETAIL)));
			MapData patient = packetData.getMapData(Crypto.decrypt(in.get(Data.ValidatePatientID.PATIENT)));
			
			long uid = Long.parseLong(inpl.get(Data.ValidatePatientID.Details.UID));
			defimpl.refreshTimer(uid);
			String personalID = patient.get(Data.ValidatePatientID.Patient.PERSONAL_ID);
			servlet.core.interfaces.Locale loc = Implementations.Locale();
			
			return db.getUser(um.nameForUID(uid)) != null
					&& loc.formatPersonalID(personalID) != null;
		}
	}
	
	private enum AddQuestionnaireAnswers implements LoggedInRequestProcesser {
		instance;

		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.ADD_QANS);

			MapData data = packetData.getMapData();
			Data.AddQuestionnaireAnswers.Response result = Data.AddQuestionnaireAnswers.Response.FAIL;
			try {
				if (storeQestionnaireAnswers(packetData.getMapData(in.get(DATA)))) { result = Data.AddQuestionnaireAnswers.Response.SUCCESS; }
			} catch (Exception e) { }
			data.put(Data.AddQuestionnaireAnswers.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private boolean storeQestionnaireAnswers(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.DETAILS)));
			MapData patient = packetData.getMapData(Crypto.decrypt(in.get(Data.AddQuestionnaireAnswers.PATIENT)));
			
			long uid = Long.parseLong(inpl.get(Data.AddQuestionnaireAnswers.Details.UID));
			defimpl.refreshTimer(uid);
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
			ListData m = packetData.getListData(in.get(Data.AddQuestionnaireAnswers.QUESTIONS));
			for (String str : m.iterable())
				answers.add(qdbf.getDBFormat(packetData.getMapData(str)));
			
			return db.addPatient(clinic_id, identifier)
					&& db.addQuestionnaireAnswers(clinic_id, identifier, answers);
		}
	}
	
	private enum SetPassword implements LoggedInRequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.SET_PASSWORD);

			MapData data = packetData.getMapData();
			Data.SetPassword.Response result = Data.SetPassword.Response.ERROR;
			try {
				result = storePassword(packetData.getMapData(in.get(DATA)));
			} catch (Exception e) { }
			data.put(Data.SetPassword.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private Data.SetPassword.Response storePassword(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.SetPassword.DETAILS)));
			long uid = Long.parseLong(inpl.get(Data.SetPassword.Details.UID));
			defimpl.refreshTimer(uid);
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
	}

	private enum LoadQuestions implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.LOAD_Q);

			MapData data = packetData.getMapData();
			String result = packetData.getMapData().toString();
			try {
				result = retrieveQuestions().toString();
			} catch (Exception e) { }
			data.put(Data.LoadQuestions.QUESTIONS, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private MapData retrieveQuestions() throws Exception {
			Map<Integer, QuestionData> questions = db.loadQuestions();
			MapData _questions = packetData.getMapData();
			for (Entry<Integer, QuestionData> _e : questions.entrySet()) {
				QuestionData _q = _e.getValue();
				MapData _question = packetData.getMapData();
				ListData options = packetData.getListData();
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
	}

	private enum LoadQResultDates implements LoggedInRequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.LOAD_QR_DATE);

			MapData data = packetData.getMapData();
			String result = packetData.getListData().toString();
			try {
				result = retrieveQResultDates(packetData.getMapData(in.get(DATA))).toString();
			} catch (Exception e) {
				logger.log("Error retrieveing questionnaire result dates", e);
			}
			data.put(Data.LoadQResultDates.DATES, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private ListData retrieveQResultDates(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.LoadQResultDates.DETAILS)));
			long uid = Long.parseLong(inpl.get(Data.LoadQResultDates.Details.UID));
			defimpl.refreshTimer(uid);
			User user = db.getUser(um.nameForUID(uid));
			List<String> dlist = db.loadQuestionResultDates(user.clinic_id);

			ListData dates = packetData.getListData();
			for (String str : dlist) {
				dates.add(str);
			}
			return dates;
		}
	}

	private enum LoadQResults implements LoggedInRequestProcesser {
		instance;
		
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.LOAD_QR);

			MapData data = packetData.getMapData();
			String result = packetData.getMapData().toString();
			try {
				result = retrieveQResults(packetData.getMapData(in.get(DATA))).toString();
			} catch (Exception e) {
				logger.log("Error retrieveing questionnaire results", e);
			}
			data.put(Data.LoadQResults.RESULTS, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private MapData retrieveQResults(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.LoadQResults.DETAILS)));
			long uid = Long.parseLong(inpl.get(Data.LoadQResults.Details.UID));
			defimpl.refreshTimer(uid);
			User _user = db.getUser(um.nameForUID(uid));
			ListData questions = packetData.getListData(in.get(Data.LoadQResults.QUESTIONS));
			List<Integer> qlist = new ArrayList<Integer>();
			for (String str : questions.iterable()) {
				qlist.add(Integer.parseInt(str));
			}
			
			List<Map<Integer, String>> _results = db.loadQuestionResults(
					_user.clinic_id, qlist,
					getDate(in.get(Data.LoadQResults.BEGIN)),
					getDate(in.get(Data.LoadQResults.END)));
			
			if (_results.size() < 5) {
				logger.log(String.format("Attempted to load %d endries which is less than 5 entries from database", _results.size()));
				return packetData.getMapData();
			} else {
				StatisticsContainer container = new StatisticsContainer();
				for (Map<Integer, String> ansmap : _results) {
					for (Entry<Integer, String> e : ansmap.entrySet()) {
						container.addResult(qdbf.getQFormat(e.getKey(), e.getValue()));
					}
				}

				MapData results = packetData.getMapData();
				for (StatisticsData sd : container.getStatistics()) {
					MapData identifiersAndCount = packetData.getMapData();
					for (Entry<Object, Integer> e : sd.getIdentifiersAndCount()) {
						identifiersAndCount.put(e.getKey().toString(), e.getValue());
					}
					results.put(sd.getQuestionID(), identifiersAndCount.toString());
				}

				return results;
			}
		}
	}

	private enum RequestRegistration implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.REQ_REGISTR);

			MapData data = packetData.getMapData();
			Data.RequestRegistration.Response result = Data.RequestRegistration.Response.FAIL;
			try {
				if (sendRegistration(packetData.getMapData(in.get(DATA)))) { result = Data.RequestRegistration.Response.SUCCESS; }
			} catch (Exception e) { }
			data.put(Data.RequestRegistration.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private boolean sendRegistration(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestRegistration.DETAILS)));
			String name = inpl.get(Data.RequestRegistration.Details.NAME);
			String email = inpl.get(Data.RequestRegistration.Details.EMAIL);
			String clinic = inpl.get(Data.RequestRegistration.Details.CLINIC);
			return MailMan.sendRegReq(name, email, clinic);
		}
	}

	private enum RequestLogin implements RequestProcesser {
		instance;
		Encryption crypto = SHAEncryption.instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.REQ_LOGIN);

			MapData data = packetData.getMapData();
			Data.RequestLogin.Response response = Data.RequestLogin.Response.INVALID_DETAILS;
			Data.RequestLogin.UpdatePassword update_password = Data.RequestLogin.UpdatePassword.NO;
			String uid = Long.toString(0L);
			try {
				UserLogin ret = login(packetData.getMapData(in.get(DATA)));
				response = ret.response;
				if (ret.user.update_password) {
					update_password = Data.RequestLogin.UpdatePassword.YES;
				}
				if (Constants.equal(ret.response, Data.RequestLogin.Response.SUCCESS)) {
					uid = Long.toString(ret.uid);
				}
			} catch (Exception e) { }
			data.put(Data.RequestLogin.RESPONSE, response);
			data.put(Data.RequestLogin.UPDATE_PASSWORD, update_password);
			data.put(Data.RequestLogin.UID, uid);

			out.put(DATA, data.toString());
			return out;
		}
		
		private UserLogin login(MapData in) throws Exception {
			UserLogin ret = new UserLogin();
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestLogin.DETAILS)));
			ret.user = db.getUser(inpl.get(Data.RequestLogin.Details.USERNAME));
			if (!ret.user.passwordMatches(inpl.get(Data.RequestLogin.Details.PASSWORD))) {
				throw new NullPointerException("invalid details");
			}

			ret.uid = 0L;
			final int MAX_ATTEMPTS = 10;
			for (int i = 0; ret.uid == 0L && i < MAX_ATTEMPTS; ++i) {
				String hash = crypto.hashMessage(
						Long.toHexString(System.currentTimeMillis()),
						ret.user.name, crypto.generateNewSalt());
				long uid = Long.parseUnsignedLong(hash.substring(0, 2*Long.BYTES), 16);
				if (um.isAvailable(uid)) {
					ret.uid = uid;
					ret.response = um.addUserToListOfOnline(ret.user.name, ret.uid);
				}
			}
			if (ret.uid == 0L) {
				ret.response = Data.RequestLogin.Response.FAIL;
			}
			
			return ret;
		}
		
		private class UserLogin {
			User user;
			long uid;
			Data.RequestLogin.Response response;
		}
	}

	private enum RequestLogout implements LoggedInRequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(TYPE, Types.REQ_LOGOUT);

			MapData data = packetData.getMapData();
			Data.RequestLogout.Response result = Data.RequestLogout.Response.ERROR;
			try {
				result = logout(packetData.getMapData(in.get(DATA)));
			} catch (Exception e) { }
			data.put(Data.RequestLogout.RESPONSE, result);

			out.put(DATA, data.toString());
			return out;
		}
		
		private Data.RequestLogout.Response logout(MapData in) throws Exception {
			MapData inpl = packetData.getMapData(Crypto.decrypt(in.get(Data.RequestLogout.DETAILS)));
			long uid = Long.parseLong(inpl.get(Data.RequestLogout.Details.UID));
			defimpl.refreshTimer(uid);
			return um.delUserFromListOfOnline(uid) ? Data.RequestLogout.Response.SUCCESS : Data.RequestLogout.Response.ERROR;
		}
	}

	private enum _GetUser implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(_TYPE, AdminTypes.GET_USER);

			MapData data = packetData.getMapData();
			String result = packetData.getMapData().toString();
			try {
				result = _retrieveUser(packetData.getMapData(in.get(_DATA))).toString();
			} catch (Exception e) { }
			data.put(AdminData.AdminGetUser.USER, result);

			out.put(_DATA, data.toString());
			return out;
		}
		
		private MapData _retrieveUser(MapData in) throws Exception {
			User _user = db.getUser(in.get(AdminData.AdminGetUser.USERNAME));
			MapData user = packetData.getMapData();
			user.put(AdminData.AdminGetUser.User.CLINIC_ID, Integer.toString(_user.clinic_id));
			user.put(AdminData.AdminGetUser.User.USERNAME, _user.name);
			user.put(AdminData.AdminGetUser.User.PASSWORD, _user.password);
			user.put(AdminData.AdminGetUser.User.EMAIL, _user.email);
			user.put(AdminData.AdminGetUser.User.SALT, _user.salt);
			user.put(AdminData.AdminGetUser.User.UPDATE_PASSWORD,
					_user.update_password ? AdminData.AdminGetUser.User.UpdatePassword.YES : AdminData.AdminGetUser.User.UpdatePassword.YES);
			return user;
		}
	}

	private enum _GetClinics implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(_TYPE, AdminTypes.GET_CLINICS);

			MapData data = packetData.getMapData();
			String result = packetData.getMapData().toString();
			try {
				result = _retrieveClinics().toString();
			} catch (Exception e) { }
			data.put(AdminData.AdminGetClinics.CLINICS, result);

			out.put(_DATA, data.toString());
			return out;
		}
		
		private MapData _retrieveClinics() throws Exception {
			Map<Integer, String> _clinics = db.getClinics();
			MapData clinics = packetData.getMapData();
			for (Entry<Integer, String> e : _clinics.entrySet())
				clinics.put(e.getKey(), e.getValue());
			return clinics;
		}
	}

	private enum _AddUser implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(_TYPE, AdminTypes.ADD_USER);

			MapData data = packetData.getMapData();
			AdminData.AdminAddUser.Response result = AdminData.AdminAddUser.Response.FAIL;
			try {
				if (_storeUser(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminAddUser.Response.SUCCESS; }
			} catch (Exception e) { }
			data.put(AdminData.AdminAddUser.RESPONSE, result);

			out.put(_DATA, data.toString());
			return out;
		}
		
		private boolean _storeUser(MapData in) throws Exception {
			MapData details = packetData.getMapData(in.get(AdminData.AdminAddUser.DETAILS));
			int clinic_id = Integer.parseInt(details.get(AdminData.AdminAddUser.Details.CLINIC_ID));
			String name = details.get(AdminData.AdminAddUser.Details.NAME);
			String password = details.get(AdminData.AdminAddUser.Details.PASSWORD);
			String email = details.get(AdminData.AdminAddUser.Details.EMAIL);
			String salt = details.get(AdminData.AdminAddUser.Details.SALT);
			return db.addUser(clinic_id, name, password, email, salt);
		}
	}

	private enum _AddClinic implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(_TYPE, AdminTypes.ADD_CLINIC);

			MapData data = packetData.getMapData();
			AdminData.AdminAddClinic.Response result = AdminData.AdminAddClinic.Response.FAIL;
			try {
				if (_storeClinic(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminAddClinic.Response.SUCCESS; }
			} catch (Exception e) { }
			data.put(AdminData.AdminAddClinic.RESPONSE, result);

			out.put(_DATA, data.toString());
			return out;
		}
		
		private boolean _storeClinic(MapData in) throws Exception {
			String name = in.get(AdminData.AdminAddClinic.NAME);
			return db.addClinic(name);
		}
	}

	private enum _RespondRegistration implements RequestProcesser {
		instance;
		public MapData processRequest(MapData in) {
			MapData out = packetData.getMapData();
			out.put(_TYPE, AdminTypes.RSP_REGISTR);

			MapData data = packetData.getMapData();
			AdminData.AdminRespondRegistration.Response result = AdminData.AdminRespondRegistration.Response.FAIL;
			try {
				if (_sendRegResp(packetData.getMapData(in.get(_DATA)))) { result = AdminData.AdminRespondRegistration.Response.SUCCESS; }
			} catch (Exception e) { }
			data.put(AdminData.AdminRespondRegistration.RESPONSE, result);

			out.put(_DATA, data.toString());
			return out;
		}
		
		private boolean _sendRegResp(MapData in) throws Exception {
			MapData details = packetData.getMapData(in.get(AdminData.AdminRespondRegistration.DETAILS));
			String username = details.get(AdminData.AdminRespondRegistration.Details.USERNAME);
			String password = details.get(AdminData.AdminRespondRegistration.Details.PASSWORD);
			String email = details.get(AdminData.AdminRespondRegistration.Details.EMAIL);
			return MailMan.sendRegResp(username, password, email);
		}
	}
	
	private enum QDBFormat {
		instance;

		Database db = MySQLDatabase.instance;
		
		String getDBFormat(MapData fc) throws Exception {
			String val = null;
			if ((val = fc.get(QuestionTypes.SINGLE_OPTION)) != null) {
				return db.escapeReplace(String.format("option%d", Integer.parseInt(val)));
			} else if ((val = fc.get(QuestionTypes.MULTIPLE_OPTION)) != null) {
				List<String> lstr = new ArrayList<String>();
				for (String str : packetData.getListData(val).iterable()) {
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
		
		Statistics getQFormat(int questionID, String dbEntry)
		{
			if (dbEntry.startsWith("option")) {
				return new SingleOption(questionID, Integer.valueOf(dbEntry.substring("option".length())));
			} else if (dbEntry.startsWith("slider")) {
				return new Slider(questionID, Integer.valueOf(dbEntry.substring("slider".length())));
			} else if (db.isSQLList(dbEntry)) {
				List<String> entries = db.SQLListToJavaList(dbEntry);
                List<Integer> lint = new ArrayList<Integer>();
				if (entries.get(0).startsWith("option")) {
					for (String str : entries)
	                    lint.add(Integer.valueOf(str.substring("option".length())));
	                return new MultipleOption(questionID, lint);
				}
			} else {
				return new Area(questionID, dbEntry);
			}
			return null;
		}
	}
}
