package eu.ascetic.monitoring.api.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

//import eu.ascetic.monitoring.api.conf.Configuration;
//import eu.ascetic.monitoring.api.datamodel.Host;
//import eu.ascetic.monitoring.api.datamodel.Item;
//import eu.ascetic.monitoring.api.utils.Dictionary;
//import eu.ascetic.monitoring.api.utils.Json2ObjectMapper;

public class ZabbixClientTestIT {
	private String URL = "http://localhost:8080/zabbix/api_jsonrpc.php";
	private String username = "admin";
	private String password = "73046447cce977b10167";
	
//	@Before
//	public void setUp() {
//		Configuration.zabbixUrl = URL;
//		Configuration.zabbixUser = username;
//		Configuration.zabbixPassword = password;
//	}
//	
//	@Test
//	public void letsGoStepByStep() {
//		ArrayList<Item> items = null;
//		ZabbixClient client = new ZabbixClient();
//		String hostname = "asok10.cit.tu-berlin.de";
//		
//		//Get info from host
//		Host host = client.getHostByName(hostname);
//		
//		if (host != null){
//			try {
//				
//				// Copy and paste from Autz... ignore it... 
//				String jsonRequest = 
//						"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
//						+ "\"params\":{\"password\":\"" + password + "\","
//									+ "\"user\":\"" + username + "\"},"
//						+ "\"method\":\"user.login\","
//						+ "\"id\": 0}";
//
//				HttpResponse response = postAndGet(jsonRequest);
//				HttpEntity entity = response.getEntity();
//				ObjectMapper mapper = new ObjectMapper ();
//				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
//				String token = (String) untyped.get("result");
//				// Copy and paste from Authz
//
//				if (token != null){
//					jsonRequest = 
//							"{\"jsonrpc\":\""+ Dictionary.JSON_RPC_VERSION + "\","
//							+ "\"method\":\"item.get\","
//							+ "\"params\":{\"output\":\"extend\","
//										+ "\"hostids\":\""+ host.getHostid() +"\","
//										+ "\"sortfield\":\"name\""
//										+ "},"
//							+ "\"auth\":\"" + token + "\","
//							+ "\"id\":0}";
//
//						response = postAndGet(jsonRequest);
//						entity = response.getEntity();
//						mapper = new ObjectMapper ();
//						String responseString = EntityUtils.toString(entity);
//						System.out.println(responseString);
//						untyped = mapper.readValue(responseString, HashMap.class);
//						ArrayList result = (ArrayList) untyped.get("result");
//						System.out.println("adasdfas");
//						System.out.println("SIZE: " + result.size());
//
//						if (result != null){
//							items = new ArrayList<Item>();
//							for (int i=0; i<result.size(); i++) {
//								HashMap<String, String> hmJsonItem = (HashMap<String, String>) result.get(i);
//								System.out.println("ENTRY: " + i);
//								System.out.println("        " + Dictionary.ITEM_NAME + " value: " + hmJsonItem.get(Dictionary.ITEM_NAME));
//								System.out.println("        " + Dictionary.ITEM_DELAY + " value: " + hmJsonItem.get(Dictionary.ITEM_DELAY));
//								System.out.println("        " + Dictionary.ITEM_HOSTID + " value: " + hmJsonItem.get(Dictionary.ITEM_HOSTID));
//								System.out.println("        " + Dictionary.ITEM_ITEMID + " value: " + hmJsonItem.get(Dictionary.ITEM_ITEMID));
//								System.out.println("        " + Dictionary.ITEM_KEY + " value: " + hmJsonItem.get(Dictionary.ITEM_KEY));
//								System.out.println("        " + Dictionary.ITEM_LASTCLOCK + " value: " + hmJsonItem.get(Dictionary.ITEM_LASTCLOCK));
//								System.out.println("        " + Dictionary.ITEM_LASTVALUE + " value: " + hmJsonItem.get(Dictionary.ITEM_LASTVALUE));
//								System.out.println("        " + Dictionary.ITEM_TRENDS + " value: " + hmJsonItem.get(Dictionary.ITEM_TRENDS));
//
//								Item item = Json2ObjectMapper.getItem((HashMap<String, String>) result.get(i));				
//								items.add(item);
//							}
//						}
//						
//						System.out.println("SIZE ITEMS: " + items.size());
//				}
//				
//			} catch (Exception e) {
//				System.out.println(e);
//			}
//			
//		}
//	}
//	
//	/**
//	 * Post and get.
//	 *
//	 * @param request the request
//	 * @return the http response
//	 * @throws IOException Signals that an I/O exception has occurred.
//	 */
//	private HttpResponse postAndGet(String request) throws IOException {
//	    HttpClient client = new DefaultHttpClient();
//	    HttpPost httpPost = new HttpPost(Configuration.zabbixUrl);
//	    httpPost.setEntity(new StringEntity(request));
//	    httpPost.addHeader("Content-Type", "application/json-rpc");
//	    return client.execute(httpPost);
//	}
//	
//	//@Test
//	public void getItemsFromHost() {
//		ZabbixClient client = new ZabbixClient();
//		
//		String hostname = "asok10.cit.tu-berlin.de";
//		List<Item> itemsList = client.getItemsFromHost(hostname);
//		int index = 0;
//		if (itemsList != null && !itemsList.isEmpty()){
//			for (Item i : itemsList){
//				System.out.println("ITEM " + index + ":");
//				System.out.println("name: " + i.getName());
//				System.out.println("key: " + i.getKey());
//				System.out.println("hostid: " + i.getHostid());
//				System.out.println("delay: " + i.getDelay());
//				System.out.println("history: " + i.getHistory());
//				System.out.println("lastvalue: " + i.getLastValue());
//				System.out.println("lastclock: " + i.getLastClock());
//				index++;
//				System.out.println();
//			}
//		}
//		else {
//			System.out.println("No items available for host " + hostname);
//		}
//	}
}
