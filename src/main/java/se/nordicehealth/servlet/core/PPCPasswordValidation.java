package se.nordicehealth.servlet.core;

import se.nordicehealth.common.impl.Packet.Data;
import se.nordicehealth.servlet.impl.User;

public interface PPCPasswordValidation {
	public Data.SetPassword.Response newPassError(User user, String oldPass, String newPass1, String newPass2);
}
