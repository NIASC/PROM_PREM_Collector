/**
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
package servlet.manage;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import servlet.core.interfaces.Encryption;
import servlet.core.interfaces.Implementations;

import java.util.Scanner;

/**
 * This class is used for managing the database and is only intended
 * to be used by administrators.
 * The managing includes adding new clinics and users to the database.
 * 
 * @author Marcus Malmquist
 *
 */
public class Manage
{
	/* Public */
	
	public static void main(String[] args)
	{
		(new Manage()).runManager();
	}
	
	/**
	 * Initializes login variables.
	 */
	public Manage()
	{
		db = ServletCommunication.getInstance();
		in = new Scanner(System.in);
	}
	
	/**
	 * Runs the manager main loop. The administrator is presented with
	 * available management operations and can choose which one to do.
	 */
	public void runManager()
	{
		final int EXIT = 0, ADD_CLINIC = 1, ADD_USER = 2;
		mgr: while (true) {
			System.out.printf("\n~~~~ PROM/PREM Manager ~~~~\n");
			System.out.printf("\nWhat would you like to do?\n"
					+ "%d: %s\n%d: %s\n%d: %s\n",
					ADD_CLINIC, "Add Clinic",
					ADD_USER, "Add user",
					EXIT, "Exit");
			if (!in.hasNextInt()) {
				in.next();
				System.out.printf("Unknown option.\n\n");
				continue;
			}
			switch (in.nextInt()) {
			case EXIT:
				break mgr;
			case ADD_CLINIC:
				addClinic();
				break;
			case ADD_USER:
				addUser();
				break;
			default:
				System.out.printf("Unknown option.\n\n");
				continue;
			}
		}
	}
	
	/* Protected */
	
	/* Private */
	
	/**
	 * Generates a first password. The password contains 8 characters
	 * from the groups:<br>
	 * ABCDEFGHIJKLMNOPQRSTUVWXYZ<br>
	 * abcdefghijklmnopqrstuvwxyz<br>
	 * !, ?, =, #, $, &amp;, @, (, ), [, ], {, }, &lt;, &gt;<br>
	 * 0123456789<br>
	 * 
	 * @return A password of length 8 with randomly generated characters.
	 */
	private String generateFirstPassword()
	{
		char[] punct = "!?=#$&@()[]{}<>".toCharArray();
		char[] upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		char[] lower = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		char[] digit = "0123456789".toCharArray();
		char[][] valid = {punct, upper, lower, digit};
		char[] pass = new char[8];
		for (int i = 0; i < 8; ++i)
		{
			char[] group = valid[(int) (valid.length * Math.random())];
			pass[i] = group[(int) (group.length * Math.random())];
		}
		return new String(pass);
	}
	
	/**
	 * Generates a username from a firstname and surname. The username will
	 * contain up to the three first characters of the firstname and surname
	 * as well as a three digit number.<br>
	 * Example:
	 * Anders Svensson might generate the username 'andsve123'<br>
	 * Bo Göransson might generate the username 'bogor789'.
	 * 
	 * @param firstname The person's firstname.
	 * @param surname The person's lastname.
	 * 
	 * @return A generated username.
	 */
	private String generateUsername(String firstname, String surname)
	{
		firstname = replaceSpecialCharacters(firstname);
		surname = replaceSpecialCharacters(surname);
		firstname = firstname.replaceAll("[^a-z]", "");
		surname = surname.replaceAll("[^a-z]", "");
		
		String username = firstname.substring(0, firstname.length() < 3 ? firstname.length() : 3)
				+ surname.substring(0, surname.length() < 3 ? surname.length() : 3)
				+ String.format("%03d", (int) (1000*Math.random()));
		return username;
	}
	
	/**
	 * Replaces special characters with their ASCII 'equivalent'.<br>
	 * Example: ö -> o, å -> a.
	 * 
	 * @param str The String to replace special characters.
	 * 
	 * @return A String where special characters have been replaced with
	 * 		their ASCII 'equivalent'.
	 */
	private String replaceSpecialCharacters(String str)
	{
		String alike = new String(new char[]{
				(char) 224, /* LATIN SMALL LETTER A WITH GRAVE */
				(char) 225, /* LATIN SMALL LETTER A WITH ACUTE  */
				(char) 226, /* LATIN SMALL LETTER A WITH CIRCUMFLEX */
				(char) 227, /* LATIN SMALL LETTER A WITH TILDE */
				(char) 228, /* LATIN SMALL LETTER A WITH DIAERESIS */
				(char) 229, /* LATIN SMALL LETTER A WITH RING ABOVE */
				(char) 230, /* LATIN SMALL LETTER AE */
		});

		String clike = new String(new char[]{
				(char) 231, /* LATIN SMALL LETTER C WITH CEDILLA */
		});

		String elike = new String(new char[]{
				(char) 232, /* LATIN SMALL LETTER E WITH GRAVE */
				(char) 233, /* LATIN SMALL LETTER E WITH ACUTE */
				(char) 234, /* LATIN SMALL LETTER E WITH CIRCUMFLEX */
				(char) 235, /* LATIN SMALL LETTER E WITH DIAERESIS */
		});

		String ilike = new String(new char[]{
				(char) 236, /* LATIN SMALL LETTER I WITH GRAVE */
				(char) 237, /* LATIN SMALL LETTER I WITH ACUTE */
				(char) 238, /* LATIN SMALL LETTER I WITH CIRCUMFLEX */
				(char) 239, /* LATIN SMALL LETTER I WITH DIAERESIS */
		});

		String dlike = new String(new char[]{
				(char) 240, /* LATIN SMALL LETTER ETH */
		});

		String nlike = new String(new char[]{
				(char) 241, /* LATIN SMALL LETTER N WITH TILDE */
		});

		String olike = new String(new char[]{
				(char) 242, /* LATIN SMALL LETTER O WITH GRAVE */
				(char) 243, /* LATIN SMALL LETTER O WITH ACUTE */
				(char) 244, /* LATIN SMALL LETTER O WITH CIRCUMFLEX */
				(char) 245, /* LATIN SMALL LETTER O WITH TILDE */
				(char) 246, /* LATIN SMALL LETTER O WITH DIAERESIS */
				(char) 248, /* LATIN SMALL LETTER O WITH STROKE */
		});

		String ulike = new String(new char[] {
				(char) 249, /* LATIN SMALL LETTER U WITH GRAVE */
				(char) 250, /* LATIN SMALL LETTER U WITH ACUTE */
				(char) 251, /* LATIN SMALL LETTER U WITH CIRCUMFLEX */
				(char) 252, /* LATIN SMALL LETTER U WITH DIAERESIS */
		});

		return str.toLowerCase()
				.replaceAll(String.format("[%s]", alike), "a")
				.replaceAll(String.format("[%s]", clike), "c")
				.replaceAll(String.format("[%s]", elike), "e")
				.replaceAll(String.format("[%s]", ilike), "i")
				.replaceAll(String.format("[%s]", dlike), "d")
				.replaceAll(String.format("[%s]", nlike), "n")
				.replaceAll(String.format("[%s]", olike), "o")
				.replaceAll(String.format("[%s]", ulike), "u");
	}
	
	/**
	 * Adds a new clinic to the database. The administrator will be
	 * presented with a form to fill in and the data is formatted
	 * such that the database can add the clinic.
	 */
	private void addClinic()
	{
		System.out.printf("\n~~~~ Add clinic ~~~~\n");
		
		Map<Integer, String> clinics = db.getClinics();
		System.out.printf("\nExisting clinics:\n");
		for (Entry<Integer, String> e : clinics.entrySet())
			System.out.printf("%4d: %s\n", e.getKey(), e.getValue());
		
		System.out.printf("\nEnter new clinic:\n");
		String clinic;
		while ((clinic = in.nextLine().trim()).isEmpty())
			;
		if (Pattern.compile("[^\\p{Print}]").matcher(clinic).find()) {
			System.out.printf("Using non-ascii characters may cause trouble.\n\n");
			return;
		}
		for (String s : clinics.values())
			if (s.equals(clinic)) {
				System.out.printf("That clinic already exist.\n\n");
				return;
			}
		System.out.printf("\nAdd clinic '%s' to database?\n"
				+ "%d: Yes\n%d: No\n", clinic, 1, 0);
		if (in.hasNextInt())
			if (in.nextInt() == 1) {
				if (db.addClinic(clinic))
					System.out.printf("Clinic added\n");
				else
					System.out.printf("Error. Consult the logs for info\n");
				return;
			}
		in.nextLine();
	}
	
	/**
	 * Adds a new user to the database. The administrator will be
	 * presented with a form to fill in and the data is formatted
	 * such that the database can add the user.
	 */
	private void addUser()
	{
		System.out.printf("\n~~~~ Add user ~~~~\n");
		/* username */
		System.out.printf("\nEnter Firstname:\n");
		String firstname;
		while ((firstname = in.nextLine().trim()).isEmpty());
		System.out.printf("\nEnter Surname:\n");
		String surname;
		while ((surname = in.nextLine().trim()).isEmpty());
		
		String user = null;
		for(int i = 0; i < 100; ++i) {
			String generated = generateUsername(firstname, surname);
			if (db.getUser(generated) != null)
				continue;
			user = generated;
			break;
		}
		if (user == null) {
			/* could not automatically generat username */
			System.out.printf("Could not generate a random username.\n");
			while (user == null) {
				System.out.printf("\nEnter username:\n");
				String suggested;
				while ((suggested = in.next().trim()).isEmpty());
				if (db.getUser(suggested) != null) {
					System.out.printf("That username is not available.\n");
					continue;
				}
				user = suggested;
				break;
			}
		}
		
		/* password */
		String password = generateFirstPassword();

		/* clinic */
		Map<Integer, String> clinics = db.getClinics();
		if (clinics.size() == 0) {
			System.out.printf("There are no clinics in the database.\n"
					+ "Please add a clinic before you add a user.\n\n");
			return;
		}
		System.out.printf("\nSelect Clinic:\n");
		for (Entry<Integer, String> e : clinics.entrySet())
			System.out.printf("%d: %s\n", e.getKey(), e.getValue());
		Integer clinic = null;
		if (in.hasNextInt()) {
			int c = in.nextInt();
			if (clinics.containsKey(c))
				clinic = c;
		} else {
			in.nextLine();
			System.out.printf("No such clinic.\n\n");
			return;
		}

		/* email */
		System.out.printf("\nEnter Email:\n");
		String email;
		while ((email = in.next().trim()).isEmpty())
			;
		
		/* verify */
		System.out.printf("\nAn email with the following login details will be "
				+ "sent to '%s'\n\tUsername: %s\n\tPassword: %s\n"
				+ "%d: Yes\n%d: No\n", email, user, password, 1, 0);
		if (in.hasNextInt()) {
			if (in.nextInt() == 1) {
				Encryption crypto = Implementations.Encryption();
				String salt = crypto.getNewSalt();
				if (db.addUser(user, crypto.hashString(password, salt),
						salt, clinic, email)) {
					if (db.respondRegistration(user, password, email))
						System.out.printf("Email sent\n");
					else
						System.out.printf("Error. Consult the logs for info\n");
				} else
					System.out.printf("Error. Consult the logs for info\n");
				return;
			}
		}
		in.nextLine();
		return;
	}
	
	private Scanner in;
	private ServletCommunication db;
}
