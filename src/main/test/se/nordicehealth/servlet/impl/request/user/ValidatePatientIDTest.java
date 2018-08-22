package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.MapData;

public class ValidatePatientIDTest {
	ValidatePatientID processer;
	ReqProcUtil dbutil;
	UserRequestUtil requtil;
	
	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();
		requtil = new UserRequestUtil(dbutil);
		processer = new ValidatePatientID(dbutil.um, dbutil.db, dbutil.pd, dbutil.logger, dbutil.crypto, dbutil.locale);
	}

	@Test
	public void testProcessRequestLoggedIn() {
        String insert = processRequest(requtil.login());
        Assert.assertEquals(Packet.SUCCESS, insert);
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
        String insert = processRequest(0L);
        Assert.assertEquals(Packet.FAIL, insert);
	}

	public String processRequest(long uid) {
        MapData dataOut = dbutil.pd.getMapData();

        MapData details = dbutil.pd.getMapData();
        details.put(Packet.UID, Long.toString(uid));

        MapData pobj = dbutil.pd.getMapData();
        pobj.put(Packet.PERSONAL_ID, "640823-3234");

        dataOut.put(Packet.DETAIL, dbutil.crypto.encrypt(details.toString()));
        dataOut.put(Packet.PATIENT, dbutil.crypto.encrypt(pobj.toString()));
        return sendRequest(dataOut);
	}

	public String sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
        out.put(Packet.TYPE, Packet.VALIDATE_PID);

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
