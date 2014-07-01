package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;

// TODO: Auto-generated Javadoc
/**
 * The Class User.
 * 
 * @author David Rojo Antona - ATOS
 */

public class User {
	
	/** The userid. */
	private String userid;
	
	/** The login. */
	private String login;
	
	/** The password. */
	private String password;
	
	/** The auth. */
	private String auth;
	

	/**
	 * Instantiates a new user.
	 *
	 * @param login the login
	 * @param password the password
	 */
	public User(String login, String password){
		setLogin(login);
		setPassword(password);
	}
	
	/**
	 * Gets the userid.
	 *
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}
	
	/**
	 * Sets the userid.
	 *
	 * @param userid the new userid
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}
	
	/**
	 * Gets the login.
	 *
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * Sets the login.
	 *
	 * @param login the new login
	 */
	public void setLogin(String login) {
		this.login = login;
	}
	
	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Gets the auth.
	 *
	 * @return the auth
	 */
	public String getAuth() {
		return auth;
	}
	
	/**
	 * Sets the auth.
	 *
	 * @param auth the new auth
	 */
	public void setAuth(String auth) {
		this.auth = auth;
	}
	
	

}
