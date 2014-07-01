package eu.ascetic.asceticarchitecture.iaas.zabbixApi.rpc.client;

//The Client sessions package
//For creating URLs
import java.util.List;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;



/**
 * The Class ZabbixApiClient.
 */
public class ZabbixApiClientTest {

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
	}


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
		String hostname = "asok09.cit.tu-berlin.de";
		List<Item> itemsList = client.getItemsFromHost(hostname);
		int index = 0;
		if (itemsList != null && !itemsList.isEmpty()){
			for (Item i : itemsList){
				System.out.println("ITEM " + index + ":");
				System.out.println("name: " + i.getName());
				System.out.println("key: " + i.getKey());
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
			System.out.println("No items available for host " + hostname);
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




}
