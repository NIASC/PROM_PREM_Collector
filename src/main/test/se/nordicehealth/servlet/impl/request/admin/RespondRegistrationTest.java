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

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.servlet.LoggerForTesting;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
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
		details.put(AdminData.AdminRespondRegistration.Details.USERNAME, "phony");
		details.put(AdminData.AdminRespondRegistration.Details.PASSWORD, "s3cr3t");
		details.put(AdminData.AdminRespondRegistration.Details.EMAIL, "phony@phony.com");
		
	}

	@Test
	public void testProcessRequest() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.RSP_REGISTR);
		MapData data_in = dbutil.pd.getMapData();
		data_in.put(AdminData.AdminRespondRegistration.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data_in.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.RSP_REGISTR,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData data_out = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminRespondRegistration.Response.SUCCESS,
				Constants.getEnum(AdminData.AdminRespondRegistration.Response.values(), data_out.get(AdminData.AdminRespondRegistration.RESPONSE))));
	}

	@Test
	public void testProcessRequestInvalidEmail() {
		MapData in = dbutil.pd.getMapData();
		in.put(AdminPacket._TYPE, AdminPacket.AdminTypes.RSP_REGISTR);
		MapData data_in = dbutil.pd.getMapData();
		details.put(AdminData.AdminRespondRegistration.Details.EMAIL, "phony.com");
		data_in.put(AdminData.AdminRespondRegistration.DETAILS, details.toString());
		in.put(AdminPacket._DATA, data_in.toString());
		
		MapData out = processer.processRequest(in);
		Assert.assertTrue(Constants.equal(AdminPacket.AdminTypes.RSP_REGISTR,
				Constants.getEnum(AdminPacket.AdminTypes.values(), out.get(AdminPacket._TYPE))));
		MapData data_out = dbutil.pd.getMapData(out.get(AdminPacket._DATA));
		Assert.assertTrue(Constants.equal(AdminData.AdminRespondRegistration.Response.FAIL,
				Constants.getEnum(AdminData.AdminRespondRegistration.Response.values(), data_out.get(AdminData.AdminRespondRegistration.RESPONSE))));
	}

}
