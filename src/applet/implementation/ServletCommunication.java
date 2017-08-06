/** ServletCommunication.java
 * 
 * Copyright 2017 Marcus Malmquist
 * 
 * This file is part of PROM_PREM_Collector.
 * 
 * PROM_PREM_Collector is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * PROM_PREM_Collector is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PROM_PREM_Collector.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package applet.implementation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import applet.core.containers.MessageContainer;
import applet.core.containers.Patient;
import applet.core.containers.QuestionContainer;
import applet.core.containers.StatisticsContainer;
import applet.core.containers.User;
import applet.core.containers.QuestionContainer.Question;
import applet.core.containers.form.AreaContainer;
import applet.core.containers.form.FieldContainer;
import applet.core.containers.form.FormContainer;
import applet.core.containers.form.MultipleOptionContainer;
import applet.core.containers.form.SingleOptionContainer;
import applet.core.containers.form.SliderContainer;
import applet.core.containers.form.TimePeriodContainer;
import applet.core.interfaces.Database;
import applet.core.interfaces.Encryption;
import applet.core.interfaces.Implementations;
import applet.core.interfaces.Questions;
import common.implementation.Constants;

/**
 * This class is an example of an implementation of
 * Database_Interface. This is done using a MySQL database and a
 * MySQL Connector/J to provide a MySQL interface to Java.
 * 
 * This class is designed to be thread safe and a singleton.
 * 
 * @author Marcus Malmquist
 *
 */
@SuppressWarnings("unchecked")
public class ServletCommunication implements Database
{
	/* Public */
	
	/**
	 * Retrieves the active instance of this class.
	 * 
	 * @return The active instance of this class.
	 */
	public static synchronized ServletCommunication getDatabase()
	{
		if (database == null)
			database = new ServletCommunication();
		return database;
	}

	@Override
	public final Object clone()
			throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	@Override
	public boolean addQuestionnaireAnswers(Patient patient, List<FormContainer> answers)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_ADD_QANS);
		
		int nQuestions = Questions.getQuestions().getContainer().getSize();
		if (answers.size() != nQuestions)
			return false;

		JSONObject questions = new JSONObject();
		Map<String, String> qmap = (Map<String, String>) questions;
		int i = 0;
		for (Iterator<FormContainer> itr = answers.iterator(); itr.hasNext();)
		{
			qmap.put(String.format("`question%d`", i++),
					QDBFormat.getDBFormat(itr.next()));
		}
		
		String identifier = crypto.encryptMessage(
				patient.getForename(), patient.getPersonalNumber(),
				patient.getSurname());
		
		rmap.put("clinic_id", Integer.toString(patient.getClinicID()));
		rmap.put("identifier", identifier);
		rmap.put("questions", questions.toString());

		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		String insert = (String) ans.get(Constants.INSERT_RESULT);
		return (insert != null && insert.equals(Constants.INSERT_SUCCESS));
	}

	@Override
	public User getUser(String username)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_GET_USER);
		rmap.put("name", username);

		JSONObject ans = sendMessage(ret);
		JSONObject user = getJSONObject((String) ans.get("user"));
		Map<String, String> umap = (Map<String, String>) user;
		User usr = null;
		try {
			usr = new User(Integer.parseInt(umap.get("clinic_id")),
					umap.get("name"),
					umap.get("password"),
					umap.get("email"),
					umap.get("salt"),
					Integer.parseInt(umap.get("update_password")) != 0);
		}
		catch (NullPointerException _e) {}
		catch (NumberFormatException _e) {}
		return usr;
	}

	@Override
	public User setPassword(User currentUser, String oldPass, String newPass,
			String newSalt)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_SET_PASSWORD);
		rmap.put("name", currentUser.getUsername());
		rmap.put("old_password", oldPass);
		rmap.put("new_password", newPass);
		rmap.put("new_salt", newSalt);

		Map<String, String> amap = (Map<String, String>) sendMessage(ret);
		Map<String, String> umap = (Map<String, String>) getJSONObject(amap.get("user"));
		User usr = null;
		try {
			usr = new User(Integer.parseInt(umap.get("clinic_id")),
					umap.get("name"),
					umap.get("password"),
					umap.get("email"),
					umap.get("salt"),
					Integer.parseInt(umap.get("update_password")) != 0);
		} catch (NullPointerException _e) {}
		return usr;
	}

	@Override
	public boolean getErrorMessages(MessageContainer mc)
	{
		if (mc == null)
			return false;
		return getMessages(Constants.CMD_GET_ERR_MSG, mc);
	}

	@Override
	public boolean getInfoMessages(MessageContainer mc)
	{
		if (mc == null)
			return false;
		return getMessages(Constants.CMD_GET_INFO_MSG, mc);
	}
	
	@Override
	public boolean loadQuestions(QuestionContainer qc)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_Q);
		
		Map<String, String> amap = (Map<String, String>) sendMessage(ret);
		Map<String, String> qmap = (Map<String, String>) getJSONObject(amap.get("questions"));
		for (Entry<String, String> e : qmap.entrySet())
		{
			Map<String, String> qtnmap = (Map<String, String>) getJSONObject(e.getValue());
			List<String> options = new ArrayList<String>();
			for (int i = 0; ; ++i)
			{
				String entry = qtnmap.get(String.format("option%d", i));
				if (entry == null)
					break;
				options.add(entry);
			}
			Class<? extends FormContainer> c;
			if ((c = getContainerClass(qtnmap.get("type"))) == null)
				continue;
			qc.addQuestion(Integer.parseInt(qtnmap.get("id")), c,
					qtnmap.get("question"), qtnmap.get("description"),
					options, Integer.parseInt(qtnmap.get("optional")) != 0,
					Integer.parseInt(qtnmap.get("max_val")),
					Integer.parseInt(qtnmap.get("min_val")));
		}
		return true;
	}

	@Override
	public boolean loadQResultDates(User user, TimePeriodContainer tpc)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_QR_DATE);
		rmap.put("name", user.getUsername());

		Map<String, String> amap = (Map<String, String>) sendMessage(ret);
		List<String> dlist = (List<String>) getJSONArray(amap.get("dates"));
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for (Iterator<String> itr = dlist.iterator(); itr.hasNext();)
			{
				Calendar cal = new GregorianCalendar();
				cal.setTime(sdf.parse(itr.next()));
				tpc.addDate(cal);
			}
		}
		catch (ParseException _e)
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean loadQResults(User user, Calendar begin, Calendar end,
			List<Integer> questionIDs, StatisticsContainer container)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_LOAD_QR);

		JSONArray questions = new JSONArray();
		List<String> qlist = (List<String>) questions;
		for (Iterator<Integer> itr = questionIDs.iterator(); itr.hasNext();)
			qlist.add(String.format("question%d", itr.next()));
		rmap.put("questions", questions.toString());

		rmap.put("name", user.getUsername());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		rmap.put("begin", sdf.format(begin.getTime()));
		rmap.put("end", sdf.format(end.getTime()));
		
		
		Map<String, String> amap = (Map<String, String>) sendMessage(ret);
		List<String> rlist = (List<String>) getJSONArray(amap.get("results"));
		for (Iterator<String> itr = rlist.iterator(); itr.hasNext();)
		{
			Map<String, String> ansmap = (Map<String, String>) getJSONObject(itr.next());
			QuestionContainer qc = Questions.getQuestions().getContainer();
			for (Entry<String, String> e : ansmap.entrySet())
			{
				int qid = Integer.parseInt(e.getKey().substring("question".length()));
				Question q1 = qc.getQuestion(qid);
				container.addResult(q1, QDBFormat.getQFormat(e.getValue()));
			}
		}
		return true;
	}
	
	@Override
	public boolean requestRegistration(
			String name, String email, String clinic)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_REQ_REGISTR);
		rmap.put("name", name);
		rmap.put("email", email);
		rmap.put("clinic", clinic);
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		String insert = (String) ans.get(Constants.INSERT_RESULT);
		return (insert != null && insert.equals(Constants.INSERT_SUCCESS));
	}

	@Override
	public int requestLogin(String username, String password)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_REQ_LOGIN);

		User user = getUser(username);
		if (user == null)
			return Constants.INVALID_DETAILS;
		rmap.put("name", username);
		rmap.put("password", user.hashWithSalt(password));
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return Constants.ERROR;
		Map<String, String> amap = (Map<String, String>) ans;
		return Integer.parseInt(amap.get(Constants.LOGIN_REPONSE));
	}

	@Override
	public boolean requestLogout(String username)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", Constants.CMD_REQ_LOGOUT);
		rmap.put("name", username);
		
		JSONObject ans = sendMessage(ret);
		if (ans == null)
			return false;
		Map<String, String> amap = (Map<String, String>) ans;
		return Integer.parseInt(amap.get(Constants.LOGOUT_REPONSE)) == Constants.SUCCESS;
	}
	
	/* Protected */
	
	/* Private */

	private static ServletCommunication database;
	private Encryption crypto;
	
	private JSONParser parser;
	
	/**
	 * Initializes variables and loads the database configuration.
	 * This class is a singleton and should only be instantiated once.
	 */
	private ServletCommunication()
	{
		crypto = Implementations.Encryption();
		parser = new JSONParser();
	}
	
	/**
	 * Sends a JSONObject to the servlet.
	 * 
	 * @param obj The JSONObject to send.
	 * 
	 * @return The JSONObject returned from the servlet.
	 */
	private JSONObject sendMessage(JSONObject obj)
	{
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) Constants.SERVER_URL.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			/* send message */
			OutputStream os = connection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
			// System.out.println(obj);
			osw.write(obj.toString());
			osw.flush();
			osw.close();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// System.out.println("Ok response");
			} else {
				// System.out.println("Bad response");
			}

			/* receive message */
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();
			// System.out.printf(">>%s<<\n", sb.toString());
			return getJSONObject(sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Attempts to parse {@code str} into a {@code JSONObject}.
	 * 
	 * @param str The string to be converted into a {@code JSONObject}.
	 * 
	 * @return The {@code JSONObject} representation of {@code str}, or
	 * 		{@code null} if {@code str} does not represent a
	 * 		{@code JSONObject}.
	 */
	private JSONObject getJSONObject(String str)
	{
		try
		{
			return (JSONObject) parser.parse(str);
		}
		catch (org.json.simple.parser.ParseException pe) { }
		return null;
	}
	
	/**
	 * Attempts to parse {@code str} into a {@code JSONArray}.
	 * 
	 * @param str The string to be converted into a {@code JSONArray}.
	 * 
	 * @return The {@code JSONArray} representation of {@code str}, or
	 * 		{@code null} if {@code str} does not represent a
	 *  	{@code JSONArray}.
	 */
	private JSONArray getJSONArray(String str)
	{
		try
		{
			return (JSONArray) parser.parse(str);
		}
		catch (org.json.simple.parser.ParseException pe) { }
		return null;
	}

	/**
	 * Retrieves messages from the database and places them in the
	 * {@code MessageContainer}.
	 * 
	 * @param commandName The name of the (message) table to retrieve
	 * 		messages from.
	 * @param mc The {@code MessageContainer} to put the messages in.
	 * 
	 * @return true if the messages was put in {@code mc}.
	 */
	private boolean getMessages(String commandName, MessageContainer mc)
	{
		JSONObject ret = new JSONObject();
		Map<String, String> rmap = (Map<String, String>) ret;
		rmap.put("command", commandName);
		
		Map<String, String> amap = (Map<String, String>) sendMessage(ret);
		Map<String, String> mmap = (Map<String, String>) getJSONObject(amap.get("messages"));
		try {
			for (Entry<String, String> e : mmap.entrySet())
			{
				Map<String, String> messagemap = (Map<String, String>) getJSONObject(e.getValue());
				Map<String, String> msgmap = (Map<String, String>) getJSONObject(messagemap.get("message"));
				mc.addMessage(Integer.parseInt(messagemap.get("code")),
						messagemap.get("name"), msgmap);
			}
		}
		catch (NullPointerException _e) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method converts a container type from the String
	 * representation in the database to the appropriate class
	 * representation in java.
	 * 
	 * @param type The type of container as it appears in the database
	 * 		(SingleOption, Slider, Field etc.).
	 * 
	 * @return The class representation of the supplied {@code type}.
	 * 		The classes can be acquired using isAssignableFrom.<Br>
	 * 		Example:<br>
	 * 		<code>
	 * 		if (SliderContainer.class.isAssignableFrom(getContainerClass("Slider")))
	 * 		new SliderContainer( ... );</code>
	 * 
	 * @see Class#isAssignableFrom
	 */
	private Class<? extends FormContainer> getContainerClass(String type)
	{
		if (type.equalsIgnoreCase("SingleOption"))
			return SingleOptionContainer.class;
		else if (type.equalsIgnoreCase("MultipleOption"))
			return MultipleOptionContainer.class;
		else if (type.equalsIgnoreCase("Field"))
			return FieldContainer.class;
		else if (type.equalsIgnoreCase("Slider"))
			return SliderContainer.class;
		else if (type.equalsIgnoreCase("Area"))
			return AreaContainer.class;
		else
			return null;
	}
	
	/**
	 * This class handles converting question answer formats between its
	 * database representation and its java representation.
	 * 
	 * @author Marcus Malmquist
	 *
	 */
	private static class QDBFormat
	{
		/**
		 * Converts the answer stored in {@code fc} to the format used
		 * in the database.
		 * 
		 * @param fc The container for the question which have been
		 * 		answered and should have the answer stored in the
		 * 		database.
		 * 
		 * @return The database representation for the answer in
		 * 		{@code fc}.
		 */
		static String getDBFormat(FormContainer fc)
		{
			if (fc.getEntry() == null)
				return "''";
			
			if (fc instanceof SingleOptionContainer)
			{
				SingleOptionContainer soc = (SingleOptionContainer) fc;
				return String.format("'option%d'", soc.getEntry());
			}
			else if (fc instanceof MultipleOptionContainer)
			{
				MultipleOptionContainer moc = (MultipleOptionContainer) fc;
				List<String> lstr = new ArrayList<String>();
				List<Integer> lint = new ArrayList<Integer>(moc.getEntry());
				Collections.sort(lint);
				for (Iterator<Integer> itr = lint.iterator(); itr.hasNext();)
					lstr.add(String.format("option%d", itr.next()));
				return String.format("[%s]", String.join(",", lstr));
			}
			else if (fc instanceof SliderContainer)
			{
				SliderContainer sc = (SliderContainer) fc;
				return String.format("'slider%d'", sc.getEntry());
			}
			else if (fc instanceof AreaContainer)
			{
				AreaContainer ac = (AreaContainer) fc;
				return String.format("'%s'", ac.getEntry());
			}
			else
				return "''";
		}
		
		/**
		 * Converts the answer {@code dbEntry} from its database
		 * representation to its java representation. The return type
		 * is {@code Object} to keep the formats general. The returned
		 * objects are in the format they need to be in order to
		 * represent the answer in its java format.
		 * 
		 * @param dbEntry The database entry that is to be converted
		 * 		to a java entry.
		 * 
 		 * @return The {@code Object} representation of the answer.
		 */
		static Object getQFormat(String dbEntry)
		{
			if (dbEntry == null || dbEntry.trim().isEmpty())
				return null;
			
			if (dbEntry.startsWith("option"))
			{ /* single option */
				return new Integer(dbEntry.substring("option".length()));
			}
			else if (dbEntry.startsWith("slider"))
			{ /* slider */
				return new Integer(dbEntry.substring("slider".length()));
			}
			else if (dbEntry.startsWith("[") && dbEntry.endsWith("]"))
			{ /* multiple answers */
				List<String> entries = Arrays.asList(dbEntry.split(","));
				if (entries.get(0).startsWith("option"))
				{ /* multiple option */
					List<Integer> lint = new ArrayList<Integer>();
					for (Iterator<String> itr = entries.iterator(); itr.hasNext();)
						lint.add(new Integer(itr.next().substring("option".length())));
					return lint;
				}
			}
			else
			{ /* must be plain text entry */
				return dbEntry;
			}
			return null;
		}
	}
}