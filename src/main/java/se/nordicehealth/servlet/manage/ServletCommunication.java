package se.nordicehealth.servlet.manage;

import static se.nordicehealth.servlet.impl.AdminPacket._ADMIN;
import static se.nordicehealth.servlet.impl.AdminPacket._DATA;
import static se.nordicehealth.servlet.impl.AdminPacket._TYPE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import se.nordicehealth.common.impl.Constants;
import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.AdminPacket.Admin;
import se.nordicehealth.servlet.impl.AdminPacket.AdminData;
import se.nordicehealth.servlet.impl.AdminPacket.AdminTypes;

public class ServletCommunication {
	
	public ServletCommunication(PPCStringScramble crypto, URL url) {
		this.crypto = crypto;
		this.local_url = url;
	}

	public boolean addUser(String username, String password, String salt, int clinic, String email)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, AdminTypes.ADD_USER);
		out.put(_ADMIN, Admin.YES);
		JSONMapData outData = new JSONMapData();

		JSONMapData details = new JSONMapData();
		details.put(AdminData.AdminAddUser.Details.CLINIC_ID, Integer.toString(clinic));
		details.put(AdminData.AdminAddUser.Details.NAME, username);
		details.put(AdminData.AdminAddUser.Details.PASSWORD, password);
		details.put(AdminData.AdminAddUser.Details.EMAIL, email);
		details.put(AdminData.AdminAddUser.Details.SALT, salt);

		outData.put(AdminData.AdminAddUser.DETAILS, details.toString());
		
		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		AdminData.AdminAddUser.Response insert = AdminData.AdminAddUser.Response.FAIL;
		try {
			insert = Constants.getEnum(AdminData.AdminAddUser.Response.values(), inData.get(AdminData.AdminAddUser.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(AdminData.AdminAddUser.Response.SUCCESS, insert);
	}

	public boolean addClinic(String clinicName)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, AdminTypes.ADD_CLINIC);
		out.put(_ADMIN, Admin.YES);
		JSONMapData outData = new JSONMapData();
		
		outData.put(AdminData.AdminAddClinic.NAME, clinicName);

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		AdminData.AdminAddClinic.Response insert = AdminData.AdminAddClinic.Response.FAIL;
		try {
			insert = Constants.getEnum(AdminData.AdminAddClinic.Response.values(), inData.get(AdminData.AdminAddClinic.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(AdminData.AdminAddClinic.Response.SUCCESS, insert);
	}
	
	public Map<Integer, String> getClinics()
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, AdminTypes.GET_CLINICS);
		out.put(_ADMIN, Admin.YES);
		JSONMapData outData = new JSONMapData();

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		JSONMapData _clinics = new JSONMapData(inData.get(AdminData.AdminGetClinics.CLINICS));
		
		Map<Integer, String> clinic = new TreeMap<Integer, String>();
		for (Entry<String, String> e : _clinics.jmap.entrySet()) {
			clinic.put(Integer.parseInt(e.getKey()), e.getValue());
		}
		return clinic;
	}
	
	public User getUser(String username)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, AdminTypes.GET_USER);
		out.put(_ADMIN, Admin.YES);
		JSONMapData outData = new JSONMapData();
		
		outData.put(AdminData.AdminGetUser.USERNAME, username);

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		JSONMapData _user = new JSONMapData(inData.get(AdminData.AdminGetUser.USER));
		try {
			User _usr = new User(crypto);
			_usr.clinic_id = Integer.parseInt(_user.get(AdminData.AdminGetUser.User.CLINIC_ID));
			_usr.name = _user.get(AdminData.AdminGetUser.User.USERNAME);
			_usr.password = _user.get(AdminData.AdminGetUser.User.PASSWORD);
			_usr.email = _user.get(AdminData.AdminGetUser.User.EMAIL);
			_usr.salt = _user.get(AdminData.AdminGetUser.User.SALT);
			AdminData.AdminGetUser.User.UpdatePassword up = Constants.getEnum(AdminData.AdminGetUser.User.UpdatePassword.values(),
					_user.get(AdminData.AdminGetUser.User.UPDATE_PASSWORD));
			_usr.update_password = Constants.equal(AdminData.AdminGetUser.User.UpdatePassword.YES, up);
			return _usr;
		} catch (NullPointerException _e) { return null; } catch (NumberFormatException _e) { return null; }
	}
	
	public boolean respondRegistration(String username, String password, String email)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, AdminTypes.RSP_REGISTR);
		out.put(_ADMIN, Admin.YES);
		JSONMapData outData = new JSONMapData();

		JSONMapData details = new JSONMapData();
		details.put(AdminData.AdminRespondRegistration.Details.USERNAME, username);
		details.put(AdminData.AdminRespondRegistration.Details.PASSWORD, password);
		details.put(AdminData.AdminRespondRegistration.Details.EMAIL, email);

		outData.put(AdminData.AdminRespondRegistration.DETAILS, details.toString());

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		AdminData.AdminRespondRegistration.Response insert = AdminData.AdminRespondRegistration.Response.FAIL;
		try {
			insert = Constants.getEnum(AdminData.AdminRespondRegistration.Response.values(), inData.get(AdminData.AdminRespondRegistration.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(AdminData.AdminRespondRegistration.Response.SUCCESS, insert);
	}

	private URL local_url;
	private PPCStringScramble crypto;
	private static JSONParser parser;
	
	static {
		parser = new JSONParser();
	}
	
	private JSONMapData sendMessage(JSONMapData obj)
	{
		try {
			HttpURLConnection c = setupHttpConnection();
			String request = obj.toString();
			//System.out.printf("OUT: '%s'\n", request);
			sendRequest(c, request);
			String response = receiveResponse(c);
			//System.out.printf(" IN: '%s'\n", response);
			return new JSONMapData(response);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private String receiveResponse(HttpURLConnection c) throws UnsupportedEncodingException, IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		for (String inputLine; (inputLine = in.readLine()) != null; sb.append(inputLine));
		in.close();
		return sb.toString();
	}

	private void sendRequest(HttpURLConnection c, String request) throws UnsupportedEncodingException, IOException {
		OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
		out.write(request);
		out.flush();
		out.close();
	}

	private HttpURLConnection setupHttpConnection() throws IOException, ProtocolException {
		HttpURLConnection c = (HttpURLConnection) local_url.openConnection();
		c.setRequestMethod("POST");
		c.setRequestProperty("Content-Type", "application/json");
		c.setUseCaches(false);
		c.setDoInput(true);
		c.setDoOutput(true);
		return c;
	}
	
	private static JSONObject getJSONObject(String str)
	{
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(str);
		} catch (Exception ignored) { }
		return obj;
	}
	
	private static class JSONMapData
	{
		JSONObject jobj;
		Map<String, String> jmap;
		
		JSONMapData() {
			this((JSONObject) null);
		}
		
		JSONMapData(String str) {
			this(getJSONObject(str));
		}
		
		@SuppressWarnings("unchecked")
		JSONMapData(JSONObject jobj) {
			this.jobj = jobj != null ? jobj : new JSONObject();
			this.jmap = (Map<String, String>) this.jobj;
		}
		
		public String toString() { return jobj.toString(); }
		void put(Enum<?> k, Enum<?> v) { jmap.put(Integer.toString(k.ordinal()), Integer.toString(v.ordinal())); }
		void put(Enum<?> k, String v) { jmap.put(Integer.toString(k.ordinal()), v); }
		String get(Enum<?> k) { return jmap.get(Integer.toString(k.ordinal())); }
	}
}