package servlet.manage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import servlet.core.interfaces.Encryption;
import servlet.implementation.SHAEncryption;

import java.util.Scanner;

public class Manage
{
	public static void main(String[] args) {
		SecureRandom sr = null;
		MessageDigest md = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.printf("FATAL: Hashing algorithms %s and/or %s is not available.\n", "SHA1PRNG", "SHA-256");
			System.exit(1);
		}
		Encryption crypto = new SHAEncryption(sr, md);
		ServletCommunication scom = new ServletCommunication(crypto);
		Scanner in = new Scanner(System.in);
		new Manage(scom, crypto, in).runManager();
	}
	
	public Manage(ServletCommunication scom, Encryption crypto, Scanner in) {
		this.db = scom;
		this.in = in;
		this.crypto = crypto;
	}
	
	public void runManager()
	{
		final int EXIT = 0, ADD_CLINIC = 1, ADD_USER = 2;
		mgr: while (true) {
			printfTitle("\n~~~~ PROM/PREM Manager ~~~~\n");
			printfQuestion("\nWhat would you like to do?\n"
					+ "%s\n%s\n%s\n",
					option(ADD_CLINIC, "Add Clinic"),
					option(ADD_USER, "Add user"),
					option(EXIT, "Exit"));
			if (!in.hasNextInt()) {
				in.next();
				printfError("Unknown option.\n\n");
				continue;
			}
			switch (in.nextInt()) {
			case EXIT:
				break mgr;
			case ADD_CLINIC:
				newClinic();
				break;
			case ADD_USER:
				newUser();
				break;
			default:
				printfError("Unknown option.\n\n");
				continue;
			}
		}
	}
	
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
	 * Example: ö -&gt; o, å -&gt; a.
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
	
	private void newClinic()
	{
		printfTitle("\n~~~~ Add clinic ~~~~\n");
		
		Map<Integer, String> clinics = db.getClinics();
		printfQuestion("\nExisting clinics:\n");
		for (Entry<Integer, String> e : clinics.entrySet())
			printf("%s\n", option(e.getKey(), e.getValue()));
		
		printfQuestion("\nEnter new clinic:\n");
		String clinic;
		while ((clinic = in.nextLine().trim()).isEmpty())
			;
		if (Pattern.compile("[^\\p{Print}]").matcher(clinic).find()) {
			printfError("Using non-ascii characters may cause trouble.\n\n");
			return;
		}
		for (String s : clinics.values())
			if (s.equals(clinic)) {
				printfError("That clinic already exist.\n\n");
				return;
			}
		printfQuestion("\nAdd clinic '%s' to database?\n%s\n%s\n",
				clinic, option(1, "Yes"), option(0, "No"));
		if (in.hasNextInt())
			if (in.nextInt() == 1) {
				if (db.addClinic(clinic))
					printfConfirm("Clinic added\n");
				else
					printfError("Error. Consult the logs for info\n");
				return;
			}
		in.nextLine();
	}
	
	private void newUser()
	{
		printfTitle("\n~~~~ Add user ~~~~\n");
		/* username */
		printfQuestion("\nEnter Firstname:\n");
		String firstname;
		while ((firstname = in.nextLine().trim()).isEmpty());
		printfQuestion("\nEnter Surname:\n");
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
			printfError("Could not generate a random username.\n");
			while (user == null) {
				printfQuestion("\nEnter username:\n");
				String suggested;
				while ((suggested = in.next().trim()).isEmpty());
				if (db.getUser(suggested) != null) {
					printfError("That username is not available.\n");
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
			printfError("There are no clinics in the database.\n"
					+ "Please add a clinic before you add a user.\n\n");
			return;
		}
		printfQuestion("\nSelect Clinic:\n");
		for (Entry<Integer, String> e : clinics.entrySet())
			printf("%s\n", option(e.getKey(), e.getValue()));
		Integer clinic = null;
		if (in.hasNextInt()) {
			int c = in.nextInt();
			if (clinics.containsKey(c))
				clinic = c;
		} else {
			in.nextLine();
			printfError("No such clinic.\n\n");
			return;
		}

		/* email */
		printfQuestion("\nEnter Email:\n");
		String email;
		while ((email = in.next().trim()).isEmpty());
		
		/* verify */
		printfQuestion("\nAn email with the following login details will be "
				+ "sent to '%s'\n\tUsername: %s\n\tPassword: %s\n"
				+ "%d: Yes\n%d: No\n", email, user, password, 1, 0);
		if (in.hasNextInt()) {
			if (in.nextInt() == 1) {
				String salt = crypto.generateNewSalt();
				if (db.addUser(user, crypto.hashMessage(password, salt),
						salt, clinic, email)) {
					if (db.respondRegistration(user, password, email))
						printfConfirm("Email sent\n");
					else
						printfError("Error. Consult the logs for info\n");
				} else
					printfError("Error. Consult the logs for info\n");
				return;
			}
		}
		in.nextLine();
		return;
	}
	
	private Scanner in;
	private ServletCommunication db;
	private Encryption crypto;
	
	private String option(int key, String description) {
		return String.format("%s: %s",
				TColor.color(TColor.GRN, String.format("%4d", key)),
				TColor.color(TColor.CYN, description));
	}
	
	private void printf(String format, Object ... args) {
		System.out.printf(format, args);
	}
	
	private void printfQuestion(String format, Object ... args) {
		System.err.printf(TColor.color(TColor.YEL, format), args);
	}
	
	private void printfError(String format, Object ... args) {
		System.err.printf(TColor.color(TColor.RED, format), args);
	}
	
	private void printfConfirm(String format, Object ... args) {
		System.err.printf(TColor.color(TColor.GRN, format), args);
	}
	
	private void printfTitle(String format, Object ... args) {
		System.err.printf(TColor.color(TColor.GRN, format), args);
	}
	
	
	private static abstract class TColor {
		private static class tColor {
			String color;
			tColor(String color) { this.color = color; }
		}
		static final tColor NRM = new tColor("\u001B[0m");
		static final tColor RED = new tColor("\u001B[31m");
		static final tColor GRN = new tColor("\u001B[32m");
		static final tColor YEL = new tColor("\u001B[33m");
		static final tColor BLU = new tColor("\u001B[34m");
		static final tColor MAG = new tColor("\u001B[35m");
		static final tColor CYN = new tColor("\u001B[36m");
		static final tColor WHT = new tColor("\u001B[37m");
		
		static String color(tColor color, String message) {
			return color.color + message + NRM.color;
		}
	}
}
