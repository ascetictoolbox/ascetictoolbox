package eu.ascetic.asceticarchitecture.iaas.zabbixApi.rpc.client;

//The Client sessions package
//For creating URLs
import java.util.List;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;



/**
 * The Class ZabbixApiClient.
 */
public class ZabbixApiClientTest {

	/** The host name. */
	private static String hostName = "asok09";
	
	/** The item name. */
	private static String itemName = "Version of zabbix_agent(d) running";
	
	/** The limit. */
	private static int limit = 10;
	
	/** The item key. */
	private static String itemKey = "vfs.fs.size[/boot,used]";	
	
	/** The history item format. */
	private static String historyItemFormat = Dictionary.HISTORY_ITEM_FORMAT_INTEGER;
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
			ZabbixClient client = new ZabbixClient();
			insertSeparator("getAllHosts");
			testGetAllHosts(client);
			insertSeparator("getItemsFromHost");
			testItemsFromHost(client);
			insertSeparator("itemsCountFromHosts");
			testItemsCountFromHosts(client);	
			insertSeparator("getItemByNameFromHost");
			testGetItemByNameFromHost(client);
			insertSeparator("getHistoryDataByLimit");
			testGetHistoryDataByLimit(client);
			insertSeparator("getItemByKeyFromHost");
			testGetItemByKeyFromHost(client);
	}


	/**
	 * Insert separator.
	 *
	 * @param message the message
	 */
	public static void insertSeparator(String message){
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("***************  " + message + "  *********************");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
	}

	/**
	 * Test items from host.
	 *
	 * @param client the client
	 */
	public static void testItemsFromHost(ZabbixClient client){
		List<Item> itemsList = client.getItemsFromHost(hostName);
		int index = 0;
		if (itemsList != null && !itemsList.isEmpty()){
			for (Item i : itemsList){
				System.out.println("ITEM " + index + ":");
				System.out.println("name: " + i.getName());
				System.out.println("key: " + i.getKey());
				System.out.println("itemid: " + i.getItemid());
				System.out.println("hostid: " + i.getHostid());
				System.out.println("delay: " + i.getDelay());
				System.out.println("history: " + i.getHistory());
				System.out.println("lastvalue: " + i.getLastValue());
				System.out.println("lastclock: " + i.getLastClock());
				index++;
				System.out.println();
			}
		}
		else {
			System.out.println("No items available for host " + hostName);
		}
	}
	
	
	/**
	 * Test items count from hosts.
	 *
	 * @param client the client
	 */
	public static void testItemsCountFromHosts(ZabbixClient client){
		List<Host> hostsList = client.getAllHosts();
		if (hostsList != null && !hostsList.isEmpty()){
			for (Host h : hostsList){
				List<Item> itemsList = client.getItemsFromHost(h.getHost());
				if (itemsList != null){
					System.out.println(h.getHost() + ": " + itemsList.size() + " items");
				}
				else {
					System.out.println(h.getHost() + ": No items available");
				}
			}
		}
		else {
			System.out.println("No hosts available in system");
		} 
	}


	/**
	 * Test get all hosts.
	 *
	 * @param client the client
	 */
	public static void testGetAllHosts(ZabbixClient client){
		List<Host> hostsList = client.getAllHosts();
		int index = 0;
		if (hostsList != null && !hostsList.isEmpty()){
			for (Host h : hostsList){
				System.out.println("HOST " + index + ":");
				System.out.println("host: " + h.getHost());
				System.out.println("host id: " + h.getHostid());
				System.out.println("available: " + h.getAvailable());
				index++;
				System.out.println();
			}	
		}
		else {
			System.out.println("No hosts available in system");
		}		
	}
	
	
	/**
	 * Test get item by name from host.
	 *
	 * @param client the client
	 */
	public static void testGetItemByNameFromHost(ZabbixClient client){		
		Item i = client.getItemByNameFromHost(itemName, hostName);
		if (i != null){
			System.out.println("Host " + hostName + ", item " + itemName + ":");
			printItem(i);
		}
		else {
			System.out.println("No item " + itemName + " available in host " + hostName);
		}
	}
	
	/**
	 * Prints the item.
	 *
	 * @param i the item
	 */
	private static void printItem(Item i){
		System.out.println("name: " + i.getName());
		System.out.println("key: " + i.getKey());
		System.out.println("itemid: " + i.getItemid());
		System.out.println("hostid: " + i.getHostid());
		System.out.println("delay: " + i.getDelay());
		System.out.println("history: " + i.getHistory());
		System.out.println("lastvalue: " + i.getLastValue());
		System.out.println("lastclock: " + i.getLastClock());
	}
	
	
	/**
	 * Gets the txt format.
	 *
	 * @param historyItemFormat the history item format
	 * @return the txt format
	 */
	private static String getTxtFormat(String historyItemFormat){
		if (historyItemFormat.equalsIgnoreCase(Dictionary.HISTORY_ITEM_FORMAT_FLOAT)){
			return "float";
		}
		else if (historyItemFormat.equalsIgnoreCase(Dictionary.HISTORY_ITEM_FORMAT_INTEGER)){
			return "integer";
		}
		else if (historyItemFormat.equalsIgnoreCase(Dictionary.HISTORY_ITEM_FORMAT_LOG)){
			return "log";
		}
		else if (historyItemFormat.equalsIgnoreCase(Dictionary.HISTORY_ITEM_FORMAT_TEXT)){
			return "text";
		}
		else if (historyItemFormat.equalsIgnoreCase(Dictionary.HISTORY_ITEM_FORMAT_STRING)){
			return "String";
		}
		return "";
	}

	/**
	 * Test get history data by limit.
	 *
	 * @param client the client
	 */
	public static void testGetHistoryDataByLimit(ZabbixClient client){
		List<HistoryItem> historyItems = client.getHistoryDataFromItem(itemKey, hostName, historyItemFormat, limit);
		if (historyItems != null  && !historyItems.isEmpty()){
			System.out.println("HISTORY DATA FOR ITEM WITH KEY " + itemKey + " WITH FORMAT " + getTxtFormat(historyItemFormat) 
					+ " IN HOST " + hostName + ". LAST " + limit + " VALUES");
			System.out.println();
			int index = 0;
			for (HistoryItem hi : historyItems){				
				System.out.println("HistoryItem " + index + ":");
				System.out.println("hostId: " + hi.getHostid());
				System.out.println("itemId: " + hi.getItemid());
				System.out.println("clock: " + hi.getClock());
				System.out.println("ns: " + hi.getNanoseconds());
				System.out.println("value: " + hi.getValue());
				System.out.println();
				index++;
			}
		}
		else {
			System.out.println("No history data for item " + itemKey + " with format " +  getTxtFormat(historyItemFormat)+ " available in host " + hostName);
		}
	}
	
	
	/**
	 * Test get item by key from host.
	 *
	 * @param client the client
	 */
	public static void testGetItemByKeyFromHost(ZabbixClient client){		
		Item i = client.getItemByKeyFromHost(itemKey, hostName);
		if (i != null){
			System.out.println("Host " + hostName + ", itemKey " + itemKey + ":");
			printItem(i);
		}
		else {
			System.out.println("No item " + itemKey + " available in host " + hostName);
		}
	}


}
