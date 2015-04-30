package eu.ascetic.asceticarchitecture.iaas.zabbixApi.rpc.client;

//The Client sessions package
//For creating URLs
import java.util.List;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.conf.Configuration;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HostGroup;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Template;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;



/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojoa@atos.net 
 * 
 * Java representation of history item retrieved from Zabbix
 * 
 */
public class ZabbixApiClientTest {

	/** The host name. */
	private static String hostName = "c8fd1fe5-7b07-42c7-9991-0e348bad5fb3_asok09";
//	private static String hostName ="aaeaa2fe-4035-4cba-a63a-f8b6a8c99ba0_asok09";
	private static String hostName2 ="abdc85d2-fd11-4047-b3f4-a0bf86ad157d_asok09";
	
	/** The item name. */
	private static String itemName = "Power";
	
	/** The limit. */
	private static int limit = 5;
	
	/** The item key. */
	private static String itemKey = "power";
////	private static String itemKey = "vfs.fs.size[/,used]";
//	private static String itemKey = "java.wrapper.sent.test";
	
	/** The history item format. */
	private static String historyItemFormat = Dictionary.HISTORY_ITEM_FORMAT_FLOAT;
//	private static String historyItemFormat = Dictionary.HISTORY_ITEM_FORMAT_FLOAT;
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
			ZabbixClient client = new ZabbixClient();
			insertSeparator("getAllHosts");
			testGetAllHosts(client);
			insertSeparator("itemsCountFromHosts");
			testItemsCountFromHosts(client);	
			insertSeparator("getItemsFromHost");
			testItemsFromHost(client);
			insertSeparator("getItemsFromHost");
			testItemsFromHost2(client);
//			insertSeparator("getItemByNameFromHost");
//			testGetItemByNameFromHost(client);
//			insertSeparator("getHistoryDataByLimit");
//			testGetHistoryDataByLimit(client);
//			insertSeparator("getItemByKeyFromHost");
//			testGetItemByKeyFromHost(client);
//			insertSeparator("getTemplateByName");
//			testGetTemplateByName(client);
//			insertSeparator("createVM");
//			testCreateVM(client);
//			insertSeparator("deleteVM");
//			testDeleteVM(client);
//			insertSeparator("sendData");
//			testSendData(client);
			
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
	
	public static void testItemsFromHost2(ZabbixClient client){
		List<Item> itemsList = client.getItemsFromHost(hostName2);
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
			System.out.println("No items available for host " + hostName2);
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
				System.out.println("name: " + h.getName());
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
	 * Prints the host group.
	 *
	 * @param hg the hg
	 */
	private static void printHostGroup(HostGroup hg){
		System.out.println("name: " + hg.getName());
		System.out.println("groupid: " + hg.getGroupId());
	}
	
	/**
	 * Prints the template.
	 *
	 * @param t the t
	 */
	private static void printTemplate(Template t){
		System.out.println("templateid: " + t.getTemplateId());
		System.out.println("name: " + t.getName());
		System.out.println("host: " + t.getHost());
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

	/**
	 * Test get host group by name.
	 *
	 * @param client the client
	 */
	public static void testGetTemplateByName(ZabbixClient client){	
//		String hostGroupName = "Virtual Machines";
		String templateName = Configuration.osLinuxTemplateName;
		Template t = client.getTemplateByName(templateName);
		if (t != null){
			System.out.println("Template " + templateName + ":");
			printTemplate(t);
		}
		else {
			System.out.println("No template " + templateName + " available Zabbix environment");
		}
	}
	
	
	public static void testCreateVM(ZabbixClient client){
		String newHostName = "dummyVM_asceticJavaWrapper_test";
		String ipAddress = "1.1.1.1";
		boolean error = false;
		String newId = null;
		try {
			newId = client.createVM(newHostName, ipAddress);
		}
		catch(Exception e){
			error = true;
			System.out.println("Error creating new VM in Zabbix (hostname = " + newHostName + ", ipAddress = " + ipAddress + "). "
					+ "Details: " + e.getMessage());
		}
		
		if (!error && newId != null){
			System.out.println("VM " + newHostName + " with IP " + ipAddress + " created successfully. New ID = " + newId);
		}
		else {
			System.out.println("VM " + newHostName + " with IP " + ipAddress + " was not created in Zabbix. Please check log files");
		}
	}
	
	
	public static void testDeleteVM(ZabbixClient client){
		String hostName = "dummyVM_asceticJavaWrapper_test";
		boolean error = false;
		String deletedHostId = null;
		try {
			deletedHostId = client.deleteVM(hostName);
		}
		catch(Exception e){
			error = true;
			System.out.println("Error deleting VM in Zabbix (hostname = " + hostName + "). "
					+ "Details: " + e.getMessage());
		}
		
		if (!error && deletedHostId != null){
			System.out.println("VM " + hostName + " with ID " + deletedHostId + " deleted successfully");
		}
		else {
			System.out.println("VM " + hostName + " was not deleted in Zabbix. Please check log files");
		}
	}
	
	
//	public static void testSendData(ZabbixClient client){
//		boolean sent = client.pushData(hostName, itemKey, "1234");	
//	}

}
