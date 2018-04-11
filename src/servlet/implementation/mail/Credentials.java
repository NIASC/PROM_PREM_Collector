package servlet.implementation.mail;

public class Credentials {

	public Credentials(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public String getEmail() { return email; }
	public String getPassword() { return password; }
	
	private String email;
	private String password;
}
