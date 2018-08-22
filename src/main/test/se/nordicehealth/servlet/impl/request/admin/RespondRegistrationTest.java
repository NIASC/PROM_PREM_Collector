package se.nordicehealth.servlet.impl.request.admin;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.mail.Credentials;
import se.nordicehealth.servlet.impl.mail.IMailConfig;
import se.nordicehealth.servlet.impl.mail.MailMan;
import se.nordicehealth.servlet.impl.mail.MessageGenerator;
import se.nordicehealth.servlet.impl.mail.emails.RegistrationResponse;
import se.nordicehealth.servlet.impl.request.admin._RespondRegistration;
import se.nordicehealth.zzphony.email.PhonyMessage;
import se.nordicehealth.zzphony.email.PhonyTransport;

public class RespondRegistrationTest {
	_RespondRegistration processer;
	ReqProcUtil dbutil;
	
	MapData details;

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
		MailMan mm = new MailMan(_mc, new Credentials("", ""), new ArrayList<String>(0), new LoggerForTesting());
		
		String _template = "Username: %s<br>Password: %s<br>";
		String template = String.format(_template, "PPC_REGRESP_USERNAME", "PPC_REGRESP_PASSWORD");
		RegistrationResponse rr = new RegistrationResponse(new MessageGenerator(template));
		processer = new _RespondRegistration(dbutil.pd, dbutil.logger, mm, rr);

		details = dbutil.pd.getMapData();
		details.put(AdminPacket.USERNAME, "phony");
		details.put(AdminPacket.PASSWORD, "s3cr3t");
		details.put(AdminPacket.EMAIL, "phony@phony.com");
		
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._RSP_REGISTR);
		MapData data_in = dbutil.pd.getMapData();
		data_in.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data_in.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._RSP_REGISTR, out.get(AdminPacket._TYPE));
		MapData data_out = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.SUCCESS, data_out.get(AdminPacket.RESPONSE));
	}

	@Test
	public void testProcessRequestInvalidEmail() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket._RSP_REGISTR);
		MapData data_in = dbutil.pd.getMapData();
		details.put(AdminPacket.EMAIL, "phony.com");
		data_in.put(AdminPacket.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data_in.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertEquals(AdminPacket._RSP_REGISTR, out.get(AdminPacket._TYPE));
		MapData data_out = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertEquals(AdminPacket.FAIL, data_out.get(AdminPacket.RESPONSE));
	}

}
