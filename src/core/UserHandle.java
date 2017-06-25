package core;

import java.util.HashMap;
import java.util.regex.Pattern;

import implement.Database;
import implement.Encryption;
import implement.Messages;
import implement.Registration;
import implement.UserInterface;
import core.containers.User;
import core.containers.form.Field;
import core.containers.form.FieldContainer;

/**
 * This class handles the user. This mostly means handling the login,
 * logout and redirecting to the registration form.
 * The purpose of this class is to handle user-related processes.
 * 
 * @author Marcus Malmquist
 *
 */
public class UserHandle
{
	public static final int USER_FOUND = 0x10;
	public static final int DETAILS_MATCH = 0x20;
	
	private Database user_db;
	private UserInterface ui;
	private User user;
	
	private boolean loggedIn;
	
	/**
	 * Initialize variables.
	 * 
	 * @param ui The active instance of the user interface.
	 */
	public UserHandle(UserInterface ui)
	{
		this.ui = ui;
		user_db = new Database();
		user = null;
		loggedIn = false;
	}
	
	/**
	 * Prompts the user for login details and attempts to match them
	 * with data in the database.
	 * If the user successfully logs in it will be flagged as logged in
	 * (which can be used to verify that the user has logged in using
	 * the function isLoggedIn).
	 */
	public void login()
	{
		if (loggedIn)
			return;
		user_db.connect();
		HashMap<String, String> details = ui.requestLoginDetails("user", "pass");
		int ret = validateDetails(details.get("user"), details.get("pass"));
		if ((ret & (USER_FOUND | DETAILS_MATCH)) != (USER_FOUND | DETAILS_MATCH))
		{
			ui.displayError(Messages.error.getMessage(
					"UH_INVALID_LOGIN", Messages.LOCALE));
			// TODO: add functionality for resetting password.
			return;
		}
		initLoginVars();
		if (user.getUpdatePassword())
		{
			ui.displayMessage("Your account have been lagged for password update.");
			setPassword();
		}
		user_db.disconnect();
	}
	
	/**
	 * Presents a registration form that the user must fill in and
	 * sends the request to an administrator who can add the user if
	 * it is authorized.
	 */
	public void register()
	{
		if (loggedIn)
			return;
		Registration register = new Registration(ui);
		register.registrationProcess();
	}
	
	/**
	 * Resets any variables that was initialized during login (in
	 * particular it resets the 'logged in' flag).
	 */
	public void logout()
	{
		if (!loggedIn)
			return;
		System.out.printf("Logging out\n");
		resetLoginVars();
	}
	
	/**
	 * Returns the 'logged in' flag.
	 * 
	 * @return True if the user is logged in. False if not.
	 */
	public boolean isLoggedIn()
	{
		return loggedIn;
	}
	
	/**
	 * Query the user for current password and new password if the user
	 * is logged in. If the old password is correct and the password
	 * matches the valid passwords criterion.
	 * This function will query the user until the old and new password
	 * fulfill the criterion described above.
	 */
	private void setPassword()
	{
		if (!loggedIn)
			return; // no user to set password for
		
		FieldContainer fc = new FieldContainer();
		final String CP_MSG = Messages.info.getMessage(
				"CURRENT_PASSWORD", Messages.LOCALE);
		final String NP1_MSG = Messages.info.getMessage(
				"NEW_PASSWORD", Messages.LOCALE);
		final String NP2_MSG = Messages.info.getMessage(
				"RE_NEW_PASSWORD", Messages.LOCALE);
		fc.addForm(new Field(CP_MSG));
		fc.addForm(new Field(NP1_MSG));
		fc.addForm(new Field(NP2_MSG));
		final int CP_ID = fc.getID(CP_MSG), NP1_ID = fc.getID(NP1_MSG),
				NP2_ID = fc.getID(NP2_MSG);
		
		HashMap<Integer, Field> formMap = null;
		boolean match = false;
		while (!match)
		{
			ui.displayMessage(Messages.info.getMessage(
					"NEW_PASS_INFO", Messages.LOCALE));
			ui.displayForm(fc);
			formMap = fc.get();
			if (newPassError(
					formMap.get(CP_ID).getValue(),
					formMap.get(NP1_ID).getValue(),
					formMap.get(NP2_ID).getValue()))
			{
				formMap.get(CP_ID).setValue(null);
				formMap.get(NP1_ID).setValue(null);
				formMap.get(NP2_ID).setValue(null);
			}
			else
				match = true;
		}
		Encryption crypto = new Encryption();
		String newSalt = crypto.getNewSalt();
		User tmpUser = user_db.setPassword(
				user, formMap.get(CP_ID).getValue(),
				crypto.hashString(
						formMap.get(NP1_ID).getValue(), newSalt),
				newSalt);
		if (tmpUser != null)
			user = tmpUser;
		else
			ui.displayError(Messages.DATABASE_ERROR);
	}
	
	/**
	 * Attempts to find any errors in the supplied passwords.
	 * The errors it looks for are:
	 * Old/current password must match the user's current password.
	 * The two new passwords must be the same.
	 * The new password must fit the length constraint.
	 * The new password must fit the strength constraint.
	 * 
	 * @param oldPass The unhashed old/current password.
	 * @param newPass1 The first entry of the new unhashed password.
	 * @param newPass2 The second entry of the new unhashed password.
	 *  
	 * @return True if an error occurred (see description above). False
	 * 		if no errors were found.
	 */
	private boolean newPassError(
			String oldPass, String newPass1, String newPass2)
	{
		if (!user.passwordMatch(oldPass))
		{
			ui.displayError(Messages.error.getMessage(
					"UH_PR_INVALID_CURRENT", Messages.LOCALE));
			return true;
		}
		if (!newPass1.equals(newPass2))
		{
			ui.displayError(Messages.error.getMessage(
					"UH_PR_MISMATCH_NEW", Messages.LOCALE));
			return true;
		}
		int ret = validatePassword(newPass1);
		if (ret < 0)
		{
			ui.displayError(Messages.error.getMessage(
					"UH_PR_INVALID_LENGTH", Messages.LOCALE));
			return true;
		}
		else if (ret == 0)
		{
			ui.displayError(Messages.error.getMessage(
					"UH_PR_PASSWORD_SIMPLE", Messages.LOCALE));
			return true;
		}
		return false;
	}
	
	/**
	 * Attempts to match the supplied username and password with users
	 * that are already in the database. If the username and password
	 * matches the user information will be placed in the local user
	 * object.
	 * Bitwise masking can be use to mask out different return values
	 * (i.e. USER_FOUND | DETAILS_MATCH) is returned if the user is
	 * found and the password matches.
	 * 
	 * Bitwise masks:
	 * USER_FOUND		- User was found.
	 * DETAILS_MATCH	- Entered password matches the user's password
	 * 					  in the database.
	 * 
	 * @param username The name of the user to look for.
	 * @param password The password of the user to look for.
	 * @return A bitwise masking of USER_FOUND and DETAILS_MATCH
	 */
	private int validateDetails(String username, String password)
	{
		User tmp = findDetails(username);
		if (tmp == null)
			return 0;
		if (!tmp.passwordMatch(password))
			return 0;
		// username and password matches if we reach this point
		user = tmp;
		return USER_FOUND | DETAILS_MATCH;
	}
	
	/**
	 * Query the database to look for the user with the supplied name
	 * and return the instance of that user.
	 * 
	 * @param username The name of the user to look for in the
	 * 		database.
	 * @return The instance of the user with the supplied username if
	 * 		it was found. If the user was not found or an error
	 * 		occurred then null is returned.
	 */
	private User findDetails(String username)
	{
		return user_db.getUser(username);
	}
	
	/**
	 * Validates the password according to the security criterion.
	 * This should typically be done when the user wants to set a new
	 * password.
	 * The criterion are:
	 * password length: >5 and < 33 characters.
	 * password strength: contain characters from at least 2 groups
	 * 		out of [a-z], [A-Z], [0-9], [^a-zA-Z0-9] (symbols).
	 * 
	 * @param password The password to validate.
	 * @return <0 if the password does not fit the length constraint.
	 * 		0 if the password does not fit the strength constraint.
	 * 		>0 if the password fits the length constraint and strength
	 * 		constraint.
	 */
	private int validatePassword(String password)
	{
		if (password.length() < 6 || password.length() > 32)
			return -1;
		Pattern[] pattern = new Pattern[]{
				Pattern.compile("[a-z]"),
				Pattern.compile("[A-Z]"),
				Pattern.compile("[0-9]"),
				Pattern.compile("[^a-zA-Z0-9]")
		};
		int points = 0;
		if (pattern[0].matcher(password).find())
			++points;
		if (pattern[1].matcher(password).find())
			++points;
		if (pattern[2].matcher(password).find())
			++points;
		if (pattern[3].matcher(password).find())
			++points;
		return points - 1;
	}
	
	/**
	 * Initializes variables that are useful during the time the user
	 * is logged in.
	 */
	private void initLoginVars()
	{
		loggedIn = true;
	}
	
	/**
	 * Resets any variable that was initialized through initLoginVars()
	 */
	private void resetLoginVars()
	{
		loggedIn = false;
	}
}
