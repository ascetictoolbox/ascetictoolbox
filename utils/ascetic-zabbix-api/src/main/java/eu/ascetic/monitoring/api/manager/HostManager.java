package eu.ascetic.monitoring.api.manager;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClient;


/**
 * The Class HostManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class HostManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClient client;
	
	/**
	 * Instantiates a new host manager.
	 *
	 * @param c the c
	 */
	public HostManager(ZabbixApiClient c){
		client = c;
	}
}
