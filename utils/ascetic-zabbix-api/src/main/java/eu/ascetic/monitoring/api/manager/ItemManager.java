package eu.ascetic.monitoring.api.manager;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClient;

/**
 * The Class ItemManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class ItemManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClient client;
	
	/**
	 * Instantiates a new item manager.
	 *
	 * @param c the c
	 */
	public ItemManager(ZabbixApiClient c){
		client = c;
	}

}
