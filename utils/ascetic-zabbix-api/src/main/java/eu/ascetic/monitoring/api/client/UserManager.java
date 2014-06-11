package eu.ascetic.monitoring.api.client;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClientTest;

/**
 * The Class UserManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class UserManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClientTest client;
	
	/**
	 * Instantiates a new user manager.
	 *
	 * @param c the c
	 */
	public UserManager(ZabbixApiClientTest c){
		client = c;
	}
	
}
