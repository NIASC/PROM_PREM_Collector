package se.nordicehealth.servlet.implementation.requestprocessing.user;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.implementation.Packet;
import se.nordicehealth.common.implementation.Constants;
import se.nordicehealth.common.implementation.Constants.QuestionTypes;
import se.nordicehealth.common.implementation.Packet.Data;
import se.nordicehealth.servlet.implementation.io.ListData;
import se.nordicehealth.servlet.implementation.io.MapData;

public class AddQuestionnaireAnswerTest {
	AddQuestionnaireAnswers processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		
		
		processer = new AddQuestionnaireAnswers(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db,
				dbutil.qdbf, dbutil.encryption, dbutil.locale, dbutil.crypto);
	}
	
	private void setNextDatabaseUserCall(String name) {
		Map<String, Integer> ints = new HashMap<String, Integer>();
		ints.put("clinic_id", 0);
		ints.put("update_password", 1);
		dbutil.rs.setNextInts(ints);
		Map<String, String> strings = new HashMap<String, String>();
		strings.put("name", name);
		strings.put("password", "s3cr3t");
		strings.put("email", "phony@phony.com");
		strings.put("salt", "s4lt");
		dbutil.rs.setNextStrings(strings);
		dbutil.rs.setNumberOfAvailableNextCalls(1);
	}
	
	private ListData CreateQuestionsEntry() {
		ListData questions = new ListData(null);
        for (int i : new int[] {2, 0, 1}) {
        	MapData fmt = new MapData();
        	fmt.put(QuestionTypes.SINGLE_OPTION, String.format(Locale.US, "%d", i));
            questions.add(fmt.toString());
        }
        return questions;
	}
	
	private MapData createPatientEntry(String fname, String sname, String pid) {
		MapData pobj = new MapData();
        pobj.put(Data.AddQuestionnaireAnswers.Patient.FORENAME, fname);
        pobj.put(Data.AddQuestionnaireAnswers.Patient.SURNAME, sname);
        pobj.put(Data.AddQuestionnaireAnswers.Patient.PERSONAL_ID, pid);
        return pobj;
	}
	
	private MapData createUserCredentialsEntry(Long uid) {
		MapData details = new MapData();
		if (uid != null) {
			details.put(Data.AddQuestionnaireAnswers.Details.UID, Long.toString(uid));
		}
        return details;
	}
	
	private MapData createDataEntry(MapData pobj, MapData details, ListData questions) {
        MapData dataOut = new MapData();
        if (details != null) {
        	dataOut.put(Data.AddQuestionnaireAnswers.DETAILS, dbutil.crypto.encrypt(details.toString()));
        }
        if (pobj != null) {
        	dataOut.put(Data.AddQuestionnaireAnswers.PATIENT, dbutil.crypto.encrypt(pobj.toString()));
        }
        if (questions != null) {
        	dataOut.put(Data.AddQuestionnaireAnswers.QUESTIONS, questions.toString());
        }
        return dataOut;
	}

	@Test
	public void testProcessRequest() {
		MapData out = new MapData();
        out.put(Packet.TYPE, Packet.Types.ADD_QANS);

		MapData pobj = createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = createUserCredentialsEntry(1L);
		ListData questions = CreateQuestionsEntry();
        out.put(Packet.DATA, createDataEntry(pobj, details, questions).toString());

        setNextDatabaseUserCall("phony");
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        Data.AddQuestionnaireAnswers.Response insert = Data.AddQuestionnaireAnswers.Response.FAIL;
        try {
            insert = Constants.getEnum(Data.AddQuestionnaireAnswers.Response.values(), inData.get(Data.AddQuestionnaireAnswers.RESPONSE));
        } catch (NumberFormatException ignored) { }
        Assert.assertTrue(Constants.equal(Data.AddQuestionnaireAnswers.Response.SUCCESS, insert));
	}

	@Test
	public void testProcessRequestUserNotOnline() {
		MapData out = new MapData();
        out.put(Packet.TYPE, Packet.Types.ADD_QANS);

		MapData pobj = createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = createUserCredentialsEntry(1L);
		ListData questions = CreateQuestionsEntry();
        out.put(Packet.DATA, createDataEntry(pobj, details, questions).toString());

		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        Data.AddQuestionnaireAnswers.Response insert = Data.AddQuestionnaireAnswers.Response.FAIL;
        try {
            insert = Constants.getEnum(Data.AddQuestionnaireAnswers.Response.values(), inData.get(Data.AddQuestionnaireAnswers.RESPONSE));
        } catch (NumberFormatException ignored) { }
        Assert.assertTrue(Constants.equal(Data.AddQuestionnaireAnswers.Response.FAIL, insert));
	}

	@Test
	public void testProcessRequestIncompletePatient() {
		MapData out = new MapData();
        out.put(Packet.TYPE, Packet.Types.ADD_QANS);

		MapData pobj = createPatientEntry(null, null, "101010-0004");
		MapData details = createUserCredentialsEntry(1L);
		ListData questions = CreateQuestionsEntry();
        out.put(Packet.DATA, createDataEntry(pobj, details, questions).toString());

        setNextDatabaseUserCall("phony");
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        Data.AddQuestionnaireAnswers.Response insert = Data.AddQuestionnaireAnswers.Response.FAIL;
        try {
            insert = Constants.getEnum(Data.AddQuestionnaireAnswers.Response.values(), inData.get(Data.AddQuestionnaireAnswers.RESPONSE));
        } catch (NumberFormatException ignored) { }
        Assert.assertTrue(Constants.equal(Data.AddQuestionnaireAnswers.Response.FAIL, insert));
	}

	@Test
	public void testProcessRequestIncompleteUserCredentials() {
		MapData out = new MapData();
        out.put(Packet.TYPE, Packet.Types.ADD_QANS);

		MapData pobj = createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = createUserCredentialsEntry(null);
		ListData questions = CreateQuestionsEntry();
        out.put(Packet.DATA, createDataEntry(pobj, details, questions).toString());

        setNextDatabaseUserCall("phony");
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        Data.AddQuestionnaireAnswers.Response insert = Data.AddQuestionnaireAnswers.Response.FAIL;
        try {
            insert = Constants.getEnum(Data.AddQuestionnaireAnswers.Response.values(), inData.get(Data.AddQuestionnaireAnswers.RESPONSE));
        } catch (NumberFormatException ignored) { }
        Assert.assertTrue(Constants.equal(Data.AddQuestionnaireAnswers.Response.FAIL, insert));
	}

	@Test
	public void testProcessRequestEmptyPacket() {
		MapData in = dbutil.pd.getMapData();
        in.put(Packet.TYPE, Packet.Types.ADD_QANS);
		MapData data = dbutil.pd.getMapData();
		in.put(Packet.DATA, data.toString());

		MapData out = processer.processRequest(in);
		Assert.assertEquals(null, dbutil.s.getLastSQLUpdate());
		Assert.assertTrue(Constants.equal(Packet.Types.ADD_QANS,
				Constants.getEnum(Packet.Types.values(), out.get(Packet.TYPE))));
		MapData response = dbutil.pd.getMapData(out.get(Packet.DATA));
		Assert.assertTrue(Constants.equal(Data.AddQuestionnaireAnswers.Response.FAIL,
				Constants.getEnum(Data.AddQuestionnaireAnswers.Response.values(), response.get(Data.AddQuestionnaireAnswers.RESPONSE))));
	}
}
