/*! UserHandle.java
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
package servlet.core;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import common.implementation.Constants;


/**
 * This class handles the user. This mostly means handling the login,
 * logout and redirecting to the registration form as well as
 * redirecting to the different applications that are available to
 * users that have logged in.
 * 
 * @author Marcus Malmquist
 *
 */
public class PasswordHandle
{
	/* Public */
	
	/* Protected */
	
	/* Private */
	
	public static int newPassError(User user,
			String oldPass, String newPass1, String newPass2)
	{
		if (user == null || !user.passwordMatch(oldPass)) {
			return Constants.INVALID_DETAILS;
		}
		if (!newPass1.equals(newPass2)) {
			return Constants.MISMATCH_NEW;
		}
		int ret = validatePassword(newPass1);
		if (ret < 0) {
			return Constants.PASSWORD_SHORT;
		} else if (ret == 0) {
			return Constants.PASSWORD_SIMPLE;
		} else {
			return Constants.SUCCESS;
		}
	}
	
	private static int validatePassword(String password)
	{
		if (password.length() < 6 || password.length() > 32)
			return -1;
		List<Pattern> pattern = Arrays.asList(
				Pattern.compile("\\p{Lower}"), /* lowercase */
				Pattern.compile("\\p{Upper}"), /* uppercase */
				Pattern.compile("\\p{Digit}"), /* digits */
				Pattern.compile("[\\p{Punct} ]") /* punctuation and space */
		);
		if (Pattern.compile("[^\\p{Print}]").matcher(password).find())
		{ // expression contains non-ascii or non-printable characters
			return 0;
		}
		int points = 0;
		if (pattern.get(0).matcher(password).find())
			++points;
		if (pattern.get(1).matcher(password).find())
			++points;
		if (pattern.get(2).matcher(password).find())
			++points;
		if (pattern.get(3).matcher(password).find())
			++points;
		return points - 1;
	}
}
