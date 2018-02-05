package servlet.manage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import common.implementation.Constants;
import servlet.core.ServletConst;

import static servlet.core.ServletConst._Packet._ADMIN;
import static servlet.core.ServletConst._Packet._TYPE;
import static servlet.core.ServletConst._Packet._DATA;

import servlet.core.ServletConst._Packet._Admin;
import servlet.core.ServletConst._Packet._Data;
import servlet.core.ServletConst._Packet._Types;
import servlet.core.User;

public class ServletCommunication
{
	public static synchronized ServletCommunication getInstance() {
		if (database == null) { database = new ServletCommunication(); }
		return database;
	}

	@Override
	public final Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public boolean addUser(String username, String password, String salt, int clinic, String email)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, _Types.ADD_USER);
		out.put(_ADMIN, _Admin.YES);
		JSONMapData outData = new JSONMapData();

		JSONMapData details = new JSONMapData();
		details.put(_Data._AddUser.Details.CLINIC_ID, Integer.toString(clinic));
		details.put(_Data._AddUser.Details.NAME, username);
		details.put(_Data._AddUser.Details.PASSWORD, password);
		details.put(_Data._AddUser.Details.EMAIL, email);
		details.put(_Data._AddUser.Details.SALT, salt);

		outData.put(_Data._AddUser.DETAILS, details.toString());
		
		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		_Data._AddUser.Response insert = _Data._AddUser.Response.FAIL;
		try {
			insert = Constants.getEnum(_Data._AddUser.Response.values(), inData.get(_Data._AddUser.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(_Data._AddUser.Response.SUCCESS, insert);
	}

	public boolean addClinic(String clinicName)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, _Types.ADD_CLINIC);
		out.put(_ADMIN, _Admin.YES);
		JSONMapData outData = new JSONMapData();
		
		outData.put(_Data._AddClinic.NAME, clinicName);

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		_Data._AddClinic.Response insert = _Data._AddClinic.Response.FAIL;
		try {
			insert = Constants.getEnum(_Data._AddClinic.Response.values(), inData.get(_Data._AddClinic.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(_Data._AddClinic.Response.SUCCESS, insert);
	}
	
	public Map<Integer, String> getClinics()
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, _Types.GET_CLINICS);
		out.put(_ADMIN, _Admin.YES);
		JSONMapData outData = new JSONMapData();

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		JSONMapData _clinics = new JSONMapData(inData.get(_Data._GetClinics.CLINICS));
		
		Map<Integer, String> clinic = new TreeMap<Integer, String>();
		for (Entry<String, String> e : _clinics.jmap.entrySet()) {
			clinic.put(Integer.parseInt(e.getKey()), e.getValue());
		}
		return clinic;
	}
	
	public User getUser(String username)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, _Types.GET_USER);
		out.put(_ADMIN, _Admin.YES);
		JSONMapData outData = new JSONMapData();
		
		outData.put(_Data._GetUser.USERNAME, username);

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		JSONMapData _user = new JSONMapData(inData.get(_Data._GetUser.USER));
		try {
			User _usr = new User();
			_usr.clinic_id = Integer.parseInt(_user.get(_Data._GetUser.User.CLINIC_ID));
			_usr.name = _user.get(_Data._GetUser.User.USERNAME);
			_usr.password = _user.get(_Data._GetUser.User.PASSWORD);
			_usr.email = _user.get(_Data._GetUser.User.EMAIL);
			_usr.salt = _user.get(_Data._GetUser.User.SALT);
			_Data._GetUser.User.UpdatePassword up = Constants.getEnum(_Data._GetUser.User.UpdatePassword.values(),
					_user.get(_Data._GetUser.User.UPDATE_PASSWORD));
			_usr.update_password = Constants.equal(_Data._GetUser.User.UpdatePassword.YES, up);
			return _usr;
		} catch (NullPointerException | NumberFormatException _e) {
			return null;
		}
	}
	
	public boolean respondRegistration(String username, String password, String email)
	{
		JSONMapData out = new JSONMapData();
		out.put(_TYPE, _Types.RSP_REGISTR);
		out.put(_ADMIN, _Admin.YES);
		JSONMapData outData = new JSONMapData();

		JSONMapData details = new JSONMapData();
		details.put(_Data._RespondRegistration.Details.USERNAME, username);
		details.put(_Data._RespondRegistration.Details.PASSWORD, password);
		details.put(_Data._RespondRegistration.Details.EMAIL, email);

		outData.put(_Data._RespondRegistration.DETAILS, details.toString());

		out.put(_DATA, outData.toString());
		JSONMapData in = sendMessage(out);
		JSONMapData inData = new JSONMapData(in.get(_DATA));
		
		_Data._RespondRegistration.Response insert = _Data._RespondRegistration.Response.FAIL;
		try {
			insert = Constants.getEnum(_Data._RespondRegistration.Response.values(), inData.get(_Data._RespondRegistration.RESPONSE));
		} catch (NumberFormatException nfe) { }
		return Constants.equal(_Data._RespondRegistration.Response.SUCCESS, insert);
	}

	private static ServletCommunication database;
	private static JSONParser parser;
	
	static {
		parser = new JSONParser();
	}
	
	private ServletCommunication() {
	}
	
	private JSONMapData sendMessage(JSONMapData obj)
	{
		try {
			HttpURLConnection c = (HttpURLConnection) ServletConst.LOCAL_URL.openConnection();
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "application/json");
			c.setUseCaches(false);
			c.setDoInput(true);
			c.setDoOutput(true);
			
			/* send message */
			OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream(), "UTF-8");
			System.out.printf("OUT: '%s'\n", obj.toString());
			out.write(obj.toString());
			out.flush();
			out.close();

			/* receive message */
			BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (String inputLine; (inputLine = in.readLine()) != null; sb.append(inputLine));
			in.close();
			System.out.printf("IN: '%s'\n", sb.toString());
			return new JSONMapData(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
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