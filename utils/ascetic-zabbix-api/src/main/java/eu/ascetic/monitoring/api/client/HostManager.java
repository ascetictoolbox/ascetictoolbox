package eu.ascetic.monitoring.api.client;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClientTest;


/**
 * The Class HostManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class HostManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClientTest client;
	
	/**
	 * Instantiates a new host manager.
	 *
	 * @param c the c
	 */
	public HostManager(ZabbixApiClientTest c){
		client = c;
	}
}
