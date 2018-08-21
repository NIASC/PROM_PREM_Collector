package se.nordicehealth.servlet.impl;

import se.nordicehealth.servlet.core.PPCStringScramble;

public class User
{
	public int clinic_id;
	public String name;
	public String password;
	public String email;
	public String salt;
	public boolean update_password;
	
	public User(PPCStringScramble crypto) {
		this.crypto = crypto;
	}
	
	public User(PPCStringScramble crypto, int clinicID, String username, String password, String email, String salt, boolean updatePass) {
		this(crypto);
		
		this.clinic_id = clinicID;
		this.name = username;
		this.password = password;
		this.email = email;
		this.salt = salt;
		this.update_password = updatePass;
	}
	
	@Override
	public Object clone() {
		return new User(crypto, clinic_id, name, password, email, salt, update_password);
	}
	
	public boolean passwordMatches(String unhashedPass) {
		return crypto.hashMessage(unhashedPass, salt).equals(password);
	}
	
	public String hashWithSalt(String unhashedPass) {
		return crypto.hashMessage(unhashedPass, salt);
	}

	private final PPCStringScramble crypto;
}
