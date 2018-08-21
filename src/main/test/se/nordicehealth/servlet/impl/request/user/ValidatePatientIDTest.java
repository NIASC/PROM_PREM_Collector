package se.nordicehealth.servlet.impl.request.user;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Constants;
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
        Packet.Data.ValidatePatientID.Response insert = processRequest(requtil.login());
        Assert.assertTrue(Constants.equal(Packet.Data.ValidatePatientID.Response.SUCCESS, insert));
	}

	@Test
	public void testProcessRequestNotLoggedIn() {
        Packet.Data.ValidatePatientID.Response insert = processRequest(0L);
        Assert.assertTrue(Constants.equal(Packet.Data.ValidatePatientID.Response.FAIL, insert));
	}

	public Packet.Data.ValidatePatientID.Response processRequest(long uid) {
        MapData dataOut = dbutil.pd.getMapData();

        MapData details = dbutil.pd.getMapData();
        details.put(Packet.Data.ValidatePatientID.Details.UID, Long.toString(uid));

        MapData pobj = dbutil.pd.getMapData();
        pobj.put(Packet.Data.ValidatePatientID.Patient.PERSONAL_ID, "640823-3234");

        dataOut.put(Packet.Data.ValidatePatientID.DETAIL, dbutil.crypto.encrypt(details.toString()));
        dataOut.put(Packet.Data.ValidatePatientID.PATIENT, dbutil.crypto.encrypt(pobj.toString()));
        return sendRequest(dataOut);
	}

	public Packet.Data.ValidatePatientID.Response sendRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
        out.put(Packet.TYPE, Packet.Types.VALIDATE_PID);

        out.put(Packet.DATA, dataOut.toString());
		requtil.setNextDatabaseUserCall();
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
            return Constants.getEnum(Packet.Data.ValidatePatientID.Response.values(), inData.get(Packet.Data.ValidatePatientID.RESPONSE));
        } catch (NumberFormatException ignored) {
        	return Packet.Data.ValidatePatientID.Response.FAIL;
        }
	}

}
