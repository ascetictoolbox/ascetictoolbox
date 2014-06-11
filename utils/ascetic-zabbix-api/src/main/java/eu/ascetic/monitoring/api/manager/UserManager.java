package eu.ascetic.monitoring.api.manager;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClient;

/**
 * The Class UserManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class UserManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClient client;
	
	/**
	 * Instantiates a new user manager.
	 *
	 * @param c the c
	 */
	public UserManager(ZabbixApiClient c){
		client = c;
	}
	
}
