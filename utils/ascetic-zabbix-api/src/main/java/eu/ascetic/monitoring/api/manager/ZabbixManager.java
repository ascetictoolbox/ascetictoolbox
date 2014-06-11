package eu.ascetic.monitoring.api.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.datamodel.Host;
import eu.ascetic.monitoring.api.datamodel.Item;
import eu.ascetic.monitoring.api.datamodel.User;
import eu.ascetic.monitoring.api.rpc.client.ZabbixApiClient;
import eu.ascetic.monitoring.api.utils.Dictionary;

// TODO: Auto-generated Javadoc
/**
 * The Class ZabbixManager.
 * 
 * @author David Rojo Antona - ATOS
 */
public class ZabbixManager {

	/** The log. */
	Logger log = Logger.getLogger(this.getClass().getName());	
	
	/** The client. */
	ZabbixApiClient client = null;;
	
	/** The item manager. */
	ItemManager itemManager = null;
	
	/** The host manager. */
	HostManager hostManager = null;
	
	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	private ZabbixApiClient getClient(){
		if (client == null) {
			client = new ZabbixApiClient(Dictionary.ZABBIX_SERVER_URL);
		}
		return client;
	}
	
	
	/**
	 * Gets the item manager.
	 *
	 * @return the item manager
	 */
	private ItemManager getItemManager(){
		if (itemManager == null){
			itemManager = new ItemManager(getClient());
		}
		return itemManager;
	}
	
	/**
	 * Gets the host manager.
	 *
	 * @return the host manager
	 */
	private HostManager getHostManager(){
		if (hostManager == null){
			hostManager = new HostManager(getClient());
		}
		return hostManager;
	}
	
	
	/**
	 * Gets the all hosts.
	 *
	 * @param u the u
	 * @return the all hosts
	 */
	public List<Host> getAllHosts(User u){		
		ArrayList<Host> hosts = null;
		
		return hosts;
	}
	
	/**
	 * Gets the host by name.
	 *
	 * @param hostName the host name
	 * @param u the u
	 * @return the host by name
	 */
	public Host getHostByName(String hostName, User u){
		Host host = null;
		
		return null;
	}
	
	/**
	 * Gets the all items.
	 *
	 * @param u the u
	 * @return the all items
	 */
	public List<Item> getAllItems(User u){
		ArrayList<Item> items = null;
		
		return items;
	}
	
	/**
	 * Gets the items from host.
	 *
	 * @param hostName the host name
	 * @param u the u
	 * @return the items from host
	 */
	public List<Item> getItemsFromHost(String hostName, User u){
		ArrayList<Item> items = null;
		
		return items;
	}
	
	/**
	 * Gets the last value from item.
	 *
	 * @param itemName the item name
	 * @param hostName the host name
	 * @param u the u
	 * @return the last value from item
	 */
	public Item getLastValueFromItem(String itemName, String hostName, User u){
		Item item = null;
		
		return item;
	}
	
	
	/**
	 * Gets the last values from items.
	 *
	 * @param hostName the host name
	 * @param u the u
	 * @return the last values from items
	 */
	public List<Item> getLastValuesFromItems(String hostName, User u){
		ArrayList<Item> items = null;
		
		return items;
	}
	
	/**
	 * Gets the last values from item.
	 *
	 * @param numberOfValues the number of values
	 * @param itemName the item name
	 * @param hostname the hostname
	 * @param u the u
	 * @return the last values from item
	 */
	public List<Item> getLastValuesFromItem(int numberOfValues, String itemName, String hostname, User u){
		ArrayList<Item> items = null;

		return items;
	}
	

	/**
	 * Adds the host.
	 *
	 * @param host the host
	 * @param u the u
	 * @return the string
	 */
	public String addHost(Host host, User u){
		String newId = null;
		
		return newId;
	}
	
}
