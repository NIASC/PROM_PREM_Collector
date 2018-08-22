package se.nordicehealth.servlet.core;

import se.nordicehealth.servlet.impl.User;

public interface PPCPasswordValidation {
	public String newPassError(User user, String oldPass, String newPass1, String newPass2);
}
