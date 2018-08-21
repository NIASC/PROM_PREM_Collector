package se.nordicehealth.servlet.impl.request.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.servlet.impl.io.MapData;

public class UserRequestUtil {
	private ReqProcUtil dbutil;
	RequestLogin processerLogIn;
	
	UserRequestUtil(ReqProcUtil dbutil) {
		this.dbutil = dbutil;
		processerLogIn = new RequestLogin(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db, dbutil.encryption, dbutil.crypto);
	}

	public MapData createDetailsEntry() {
		MapData details = new MapData();
		details.put(Packet.Data.RequestLogin.Details.USERNAME, "12345678");
		details.put(Packet.Data.RequestLogin.Details.PASSWORD, "s3cr3t");
		return details;
	}
	
	public void setNextDatabaseDatesMapStrStr(List<Map<String, String>> dates) {
		for (Map<String, String> date : dates) {
			Map<String, String> strings = new HashMap<String, String>();
			strings.putAll(date);
			dbutil.rs.setNextStrings(strings);
			dbutil.rs.setNextInts(new HashMap<String, Integer>());
		}
		dbutil.rs.addNumberOfAvailableNextCalls(dates.size());
	}
	
	public void setNextDatabaseDatesString(List<String> dates) {
		for (String date : dates) {
			Map<String, String> strings = new HashMap<String, String>();
			strings.put("date", date);
			dbutil.rs.setNextStrings(strings);
			dbutil.rs.setNextInts(new HashMap<String, Integer>());
		}
		dbutil.rs.addNumberOfAvailableNextCalls(dates.size());
	}
	
	public MapData createUserUIDEntry(long uid) {
		MapData details = new MapData();
		details.put(Data.AddQuestionnaireAnswers.Details.UID, Long.toString(uid));
        return details;
	}
	
	public MapData createPatientEntry(String fname, String sname, String pid) {
		MapData pobj = new MapData();
		if (fname != null) {
			pobj.put(Data.AddQuestionnaireAnswers.Patient.FORENAME, fname);
		}
		if (sname != null) {
			pobj.put(Data.AddQuestionnaireAnswers.Patient.SURNAME, sname);
		}
		if (pid != null) {
			pobj.put(Data.AddQuestionnaireAnswers.Patient.PERSONAL_ID, pid);
		}
        return pobj;
	}
	
	public void setNextDatabaseUserCall() {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", "12345678");
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNextInts(ints);
		dbutil.rs.addNumberOfAvailableNextCalls(1);
	}
	
	long login() {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.Types.REQ_LOGIN);
		MapData dataOut = dbutil.pd.getMapData();
		// username has to be a number because ReqProcUtil uses PhonyEncryption
		// which does not actually hash the input into a hex string.
		dataOut.put(Packet.Data.RequestLogin.DETAILS, dbutil.crypto.encrypt(createDetailsEntry().toString()));
		out.put(Packet.DATA, dataOut.toString());
		
		setNextDatabaseUserCall();
		MapData in = processerLogIn.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

		Assert.assertTrue(Constants.equal(Data.RequestLogin.Response.SUCCESS,
				Constants.getEnum(Data.RequestLogin.Response.values(), inData.get(Data.RequestLogin.RESPONSE))));
		
		String uid = inData.get(Packet.Data.RequestLogin.UID);
		return uid != null ? Long.parseLong(uid) : 0L;
	}
}
