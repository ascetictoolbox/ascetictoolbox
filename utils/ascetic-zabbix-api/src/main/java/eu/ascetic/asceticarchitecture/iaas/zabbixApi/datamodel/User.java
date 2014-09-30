package eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel;


import java.util.Calendar;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojoa@atos.net 
 * 
 * Java representation of user retrieved from Zabbix
 * 
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
        
	private Calendar authExpiryDate = null;
	
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

    /**
     * This returns the auth expiry date.
     * @return the authExpiryDate
     */
    public Calendar getAuthExpiryDate() {
        return authExpiryDate;
    }

    /**
     * This sets the auth expiry date.
     * @param authExpiryDate the auth Expiry date to set
     */
    public void setAuthExpiryDate(Calendar authExpiryDate) {
        this.authExpiryDate = authExpiryDate;
    }
    
}
