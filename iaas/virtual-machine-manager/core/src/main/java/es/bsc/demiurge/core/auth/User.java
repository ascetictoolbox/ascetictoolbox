package es.bsc.demiurge.core.auth;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
public class User {
	public static final String ROLE_USER = "ROLE_USER";

	private String userName;
	private String cipheredPassword;
	private boolean enabled = true;

	public User() {
	}

	public User(String userName, String cipheredPassword, boolean enabled) {
		this.enabled = enabled;
		this.userName = userName;
		this.cipheredPassword = cipheredPassword;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCipheredPassword() {
		return cipheredPassword;
	}

	public void setCipheredPassword(String cipheredPassword) {
		this.cipheredPassword = cipheredPassword;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
