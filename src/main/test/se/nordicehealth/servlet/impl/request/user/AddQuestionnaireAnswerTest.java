package se.nordicehealth.servlet.impl.request.user;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.common.impl.Constants.QuestionTypes;
import se.nordicehealth.servlet.impl.io.ListData;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.request.user.AddQuestionnaireAnswers;

public class AddQuestionnaireAnswerTest {
	AddQuestionnaireAnswers processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		
		
		processer = new AddQuestionnaireAnswers(dbutil.pd, dbutil.logger, dbutil.um, dbutil.db,
				dbutil.qdbf, dbutil.encryption, dbutil.locale, dbutil.crypto);
	}
	
	private ListData CreateQuestionsEntry() {
		ListData questions = new ListData();
        for (int i : new int[] {2, 0, 1}) {
        	MapData fmt = new MapData();
        	fmt.put(QuestionTypes.SINGLE_OPTION, String.format(Locale.US, "%d", i));
            questions.add(fmt.toString());
        }
        return questions;
	}
	
	private MapData createDataEntry(MapData pobj, MapData details, ListData questions) {
        MapData dataOut = new MapData();
        if (details != null) {
        	dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));
        }
        if (pobj != null) {
        	dataOut.put(Packet.PATIENT, dbutil.crypto.encrypt(pobj.toString()));
        }
        if (questions != null) {
        	dataOut.put(Packet.QUESTIONS, questions.toString());
        }
        return dataOut;
	}

	@Test
	public void testProcessRequest() {
		MapData pobj = requtil.createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = requtil.createUserUIDEntry(requtil.login());
		ListData questions = CreateQuestionsEntry();
		String insert = sendRequest(createDataEntry(pobj, details, questions));
        Assert.assertEquals(Packet.SUCCESS, insert);
	}

	@Test
	public void testProcessRequestUserNotOnline() {
		MapData pobj = requtil.createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = requtil.createUserUIDEntry(0L);
		ListData questions = CreateQuestionsEntry();
		String insert = sendRequest(createDataEntry(pobj, details, questions));
        Assert.assertEquals(Packet.FAIL, insert);
	}

	@Test
	public void testProcessRequestIncompleteUserCredentials() {
		MapData pobj = requtil.createPatientEntry("kalle", "kula", "101010-0004");
		MapData details = dbutil.pd.getMapData();
		ListData questions = CreateQuestionsEntry();
		String insert = sendRequest(createDataEntry(pobj, details, questions));
        Assert.assertEquals(Packet.FAIL, insert);
	}

	@Test
	public void testProcessRequestIncompletePatient() {
		MapData pobj = requtil.createPatientEntry(null, null, "101010-0004");
		MapData details = requtil.createUserUIDEntry(requtil.login());
		ListData questions = CreateQuestionsEntry();
		String insert = sendRequest(createDataEntry(pobj, details, questions));
        Assert.assertEquals(Packet.FAIL, insert);
	}

	@Test
	public void testProcessRequestEmptyPacket() {
		String insert = sendRequest(dbutil.pd.getMapData());
		Assert.assertEquals(null, dbutil.s.getLastSQLUpdate());
        Assert.assertEquals(Packet.FAIL, insert);
	}

	public String sendRequest(MapData dataOut) {
		MapData out = new MapData();
        out.put(Packet.TYPE, Packet.ADD_QANS);
        out.put(Packet.DATA, dataOut.toString());
        requtil.setNextDatabaseUserCall();
		MapData in = processer.processRequest(out);
		MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
            return inData.get(Packet.RESPONSE);
        } catch (NumberFormatException ignored) {
        	return Packet.FAIL;
        }
	}
}
