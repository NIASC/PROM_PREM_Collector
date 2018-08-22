package se.nordicehealth.servlet.impl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import se.nordicehealth.common.impl.Packet;
import se.nordicehealth.servlet.core.PPCPasswordValidation;

public class PasswordHandle implements PPCPasswordValidation
{
	public String newPassError(User user,
			String oldPass, String newPass1, String newPass2)
	{
		if (user == null || !user.passwordMatches(oldPass)) {
			return Packet.INVALID_DETAILS;
		}
		if (!newPass1.equals(newPass2)) {
			return Packet.MISMATCH_NEW;
		}
		int ret = validatePassword(newPass1);
		if (ret < 0) {
			return Packet.PASSWORD_INVALID_LENGTH;
		} else if (ret == 0) {
			return Packet.PASSWORD_SIMPLE;
		} else {
			return Packet.SUCCESS;
		}
	}
	
	private int validatePassword(String password)
	{
		if (password.length() < 6 || password.length() > 32) {
			return -1;
		}
		List<Pattern> pattern = Arrays.asList(
				Pattern.compile("\\p{Lower}"), /* lowercase */
				Pattern.compile("\\p{Upper}"), /* uppercase */
				Pattern.compile("\\p{Digit}"), /* digits */
				Pattern.compile("[\\p{Punct} ]") /* punctuation and space */
		);
		if (Pattern.compile("[^\\p{Print}]").matcher(password).find()) {
			return 0; // expression contains non-ascii or non-printable characters
		}
		int points = 0;
		for (Pattern p : pattern) {
			if (p.matcher(password).find()) {
				++points;
			}
		}
		return points - 1;
	}
}
