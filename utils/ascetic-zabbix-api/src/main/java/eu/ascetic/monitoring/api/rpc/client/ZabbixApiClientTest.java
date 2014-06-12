package eu.ascetic.monitoring.api.rpc.client;

//The Client sessions package
//For creating URLs
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.monitoring.api.client.ZabbixClient;
import eu.ascetic.monitoring.api.conf.Configuration;
import eu.ascetic.monitoring.api.datamodel.Host;
import eu.ascetic.monitoring.api.datamodel.Item;



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
			ZabbixClient client = new ZabbixClient(Configuration.zabbixUser,Configuration.zabbixPassword);
			testGetAllHosts(client);
			insertSeparator();
			testItemsFromHost(client);
			insertSeparator();
	}
	
	
	public static void insertSeparator(){
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
	}
	
	public static void testItemsFromHost(ZabbixClient client){
		List<Item> itemsList = client.getItemsFromHost("Zabbix server");
		int index = 0;
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
	
	
	public static void testGetAllHosts(ZabbixClient client){
		List<Host> hostsList = client.getAllHosts();
		int index = 0;
		for (Host h : hostsList){
			System.out.println("HOST " + index + ":");
			System.out.println("host: " + h.getHost());
			System.out.println("host id: " + h.getHostid());
			System.out.println("available: " + h.getAvailable());
			index++;
			System.out.println();
		}
		
	}
	

	

}
