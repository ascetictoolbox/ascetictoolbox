package eu.ascetic.monitoring.api.client;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClientTest;

/**
 * The Class ItemManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class ItemManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());
	
	/** The client. */
	ZabbixApiClientTest client;
	
	/**
	 * Instantiates a new item manager.
	 *
	 * @param c the c
	 */
	public ItemManager(ZabbixApiClientTest c){
		client = c;
	}

}
