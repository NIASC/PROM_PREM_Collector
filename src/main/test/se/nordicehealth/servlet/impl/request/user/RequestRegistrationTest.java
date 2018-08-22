package se.nordicehealth.servlet.impl.request.user;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.mail.Credentials;
import se.nordicehealth.servlet.impl.mail.IMailConfig;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.MessageGenerator;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationRequest;
import se.nordicehealth.zzphony.email.PhonyMessage;
import se.nordicehealth.zzphony.email.PhonyTransport;

public class RequestRegistrationTest {
	RequestRegistration processer;
	ReqProcUtil dbutil;

	@Before
	public void setUp() throws Exception {
		dbutil = ReqProcUtil.newInstance();

		IMailConfig _mc = new IMailConfig() {
			@Override
			public Message createEmptyMessage() { return new PhonyMessage(); }
			@Override
			public Transport getTransport() throws NoSuchProviderException {
				return new PhonyTransport(Session.getDefaultInstance(new Properties(), null), null);
			}
		};
		MailMan mm = new MailMan(_mc, new Credentials("", ""), new ArrayList<String>(0), dbutil.logger);
		RegistrationRequest rr = new RegistrationRequest(new MessageGenerator("Name: %s<br>Email: %s<br>Clinic: %s<br>"));
		processer = new RequestRegistration(dbutil.db, dbutil.pd, dbutil.logger, mm, rr, dbutil.crypto);
	}
	
	private MapData createDetails(String name, String email, String clinic) {
		MapData details = dbutil.pd.getMapData();
        details.put(Packet.NAME, name);
        details.put(Packet.EMAIL, email);
        details.put(Packet.CLINIC, clinic);
        return details;
	}

	public String processRequest(MapData dataOut) {
		MapData out = dbutil.pd.getMapData();
		out.put(Packet.TYPE, Packet.REQ_REGISTR);
        out.put(Packet.DATA, dataOut.toString());
		MapData in = processer.processRequest(out);
        MapData inData = dbutil.pd.getMapData(in.get(Packet.DATA));

        try {
            return inData.get(Packet.RESPONSE);
        } catch (NumberFormatException ignored) {
            return Packet.FAIL;
        }
	}
	
	@Test
	public void testProcessRequestCompleteForm() {
		String name = "andrew smith";
		String email = "andrew.smith@localdomain";
		String clinic = "local clinic";
		
        MapData dataOut = dbutil.pd.getMapData();
        MapData details = createDetails(name, email, clinic);
        dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));
        String insert = processRequest(dataOut);
        Assert.assertEquals(Packet.SUCCESS, insert);
	}
	
	@Test
	public void testProcessRequestInompleteForm() {
		String name = "andrew smith";
		String email = "andrew.smith@localdomain";
		
        MapData dataOut = dbutil.pd.getMapData();
		MapData details = dbutil.pd.getMapData();
        details.put(Packet.NAME, name);
        details.put(Packet.EMAIL, email);
        dataOut.put(Packet.DETAILS, dbutil.crypto.encrypt(details.toString()));
        String insert = processRequest(dataOut);
        Assert.assertEquals(Packet.FAIL, insert);
	}

}
