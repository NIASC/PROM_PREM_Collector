package se.nordicehealth.servlet.manage;

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

import org.json.simple.parser.JSONParser;

import se.nordicehealth.servlet.core.PPCStringScramble;
import se.nordicehealth.servlet.impl.AdminPacket;
import se.nordicehealth.servlet.impl.NullLogger;
import se.nordicehealth.servlet.impl.User;
import se.nordicehealth.servlet.impl.io.MapData;
import se.nordicehealth.servlet.impl.io.PacketData;

public class ServletCommunication {
	
	private PacketData pd;
	public ServletCommunication(PPCStringScramble crypto, URL url) {
		this.crypto = crypto;
		this.local_url = url;
		pd = new PacketData(new JSONParser(), new NullLogger());
	}

	public boolean addUser(String username, String password, String salt, int clinic, String email)
	{
		MapData out = pd.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._ADD_USER);
		MapData outData = pd.getMapData();

		MapData details = pd.getMapData();
		details.put(AdminPacket.CLINIC_ID, Integer.toString(clinic));
		details.put(AdminPacket.NAME, username);
		details.put(AdminPacket.PASSWORD, password);
		details.put(AdminPacket.EMAIL, email);
		details.put(AdminPacket.SALT, salt);

		outData.put(AdminPacket.DETAILS, details.toString());
		
		out.put(AdminPacket._DATA, outData.toString());
		MapData in = sendMessage(out);
		MapData inData = pd.getMapData(in.get(AdminPacket._DATA));
		
		String insert = AdminPacket.FAIL;
		try {
			insert = inData.get(AdminPacket.RESPONSE);
		} catch (NumberFormatException nfe) { }
		return insert.equals(AdminPacket.SUCCESS);
	}

	public boolean addClinic(String clinicName)
	{
		MapData out = pd.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._ADD_CLINIC);
		MapData outData = pd.getMapData();
		
		outData.put(AdminPacket.NAME, clinicName);

		out.put(AdminPacket._DATA, outData.toString());
		MapData in = sendMessage(out);
		MapData inData = pd.getMapData(in.get(AdminPacket._DATA));
		
		String insert = AdminPacket.FAIL;
		try {
			insert = inData.get(AdminPacket.RESPONSE);
		} catch (NumberFormatException nfe) { }
		return insert.equals(AdminPacket.SUCCESS);
	}
	
	public Map<Integer, String> getClinics()
	{
		MapData out = pd.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._GET_CLINICS);
		MapData outData = pd.getMapData();

		out.put(AdminPacket._DATA, outData.toString());
		MapData in = sendMessage(out);
		MapData inData = pd.getMapData(in.get(AdminPacket._DATA));
		
		MapData _clinics = pd.getMapData(inData.get(AdminPacket.CLINICS));
		
		Map<Integer, String> clinic = new TreeMap<Integer, String>();
		for (Entry<String, String> e : _clinics.iterable()) {
			clinic.put(Integer.parseInt(e.getKey()), e.getValue());
		}
		return clinic;
	}
	
	public User getUser(String username)
	{
		MapData out = pd.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._GET_USER);
		MapData outData = pd.getMapData();
		
		outData.put(AdminPacket.USERNAME, username);

		out.put(AdminPacket._DATA, outData.toString());
		MapData in = sendMessage(out);
		MapData inData = pd.getMapData(in.get(AdminPacket._DATA));
		
		MapData _user = pd.getMapData(inData.get(AdminPacket.USER));
		try {
			User _usr = new User(crypto);
			_usr.clinic_id = Integer.parseInt(_user.get(AdminPacket.CLINIC_ID));
			_usr.name = _user.get(AdminPacket.USERNAME);
			_usr.password = _user.get(AdminPacket.PASSWORD);
			_usr.email = _user.get(AdminPacket.EMAIL);
			_usr.salt = _user.get(AdminPacket.SALT);
			_usr.update_password = _user.get(AdminPacket.UPDATE_PASSWORD).equals(AdminPacket.YES);
			return _usr;
		} catch (NullPointerException _e) { return null; } catch (NumberFormatException _e) { return null; }
	}
	
	public boolean respondRegistration(String username, String password, String email)
	{
		MapData out = pd.getMapData();
		out.put(AdminPacket._TYPE, AdminPacket._RSP_REGISTR);
		MapData outData = pd.getMapData();

		MapData details = pd.getMapData();
		details.put(AdminPacket.USERNAME, username);
		details.put(AdminPacket.PASSWORD, password);
		details.put(AdminPacket.EMAIL, email);

		outData.put(AdminPacket.DETAILS, details.toString());

		out.put(AdminPacket._DATA, outData.toString());
		MapData in = sendMessage(out);
		MapData inData = pd.getMapData(in.get(AdminPacket._DATA));
		
		String insert = AdminPacket.FAIL;
		try {
			insert = inData.get(AdminPacket.RESPONSE);
		} catch (NumberFormatException nfe) { }
		return insert.equals(AdminPacket.SUCCESS);
	}

	private URL local_url;
	private PPCStringScramble crypto;
	
	private MapData sendMessage(MapData obj)
	{
		try {
			HttpURLConnection c = setupHttpConnection();
			String request = obj.toString();
			//System.out.printf("OUT: '%s'\n", request);
			sendRequest(c, request);
			String response = receiveResponse(c);
			//System.out.printf(" IN: '%s'\n", response);
			return pd.getMapData(response);
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
}