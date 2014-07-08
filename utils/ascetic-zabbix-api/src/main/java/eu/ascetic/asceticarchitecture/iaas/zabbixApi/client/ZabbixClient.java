package eu.ascetic.asceticarchitecture.iaas.zabbixApi.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.conf.Configuration;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.User;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Json2ObjectMapper;

/**
 * The Class ZabbixClient.
 * 
 * @author David Rojo Antona - ATOS
 */
public class ZabbixClient {

	/** The log. */
	private Logger log = Logger.getLogger(this.getClass().getName());	
	
	/** The user. */
	private User user;
	

	/**
	 * Instantiates a new zabbix client. Get user data from properties file
	 */
	public ZabbixClient(){
		user = new User(Configuration.zabbixUser,Configuration.zabbixPassword);		
	}
	
	/**
	 * Instantiates a new zabbix client.
	 *
	 * @param login the login
	 * @param password the password
	 */
	public ZabbixClient(String login, String password){
		user = new User(login, password);
	}
		
	/**
	 * Gets the auth.
	 *
	 * @return the auth
	 */
	private String getAuth(){
		String auth = null;

		String jsonRequest = 
				"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
				+ "\"params\":{\"password\":\"" + user.getPassword() + "\","
							+ "\"user\":\"" + user.getLogin() + "\"},"
				+ "\"method\":\"user.login\","
				+ "\"id\": 0}";

		try {
			HttpResponse response = postAndGet(jsonRequest);
			HttpEntity entity = response.getEntity();
			ObjectMapper mapper = new ObjectMapper ();
			HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
			auth = (String) untyped.get("result");

			if (auth == null) {
				throw new IllegalArgumentException("Authorization failed to : " 
						+ Configuration.zabbixUrl + ", using login: "
						+ user.getLogin());
			}
			log.info("Successfully connected to the server\n");

		} catch (IOException e) {
			log.error("Could not connect to the Zabbix Server at : " 
					+ Configuration.zabbixUrl + ". Exception: " 
					+ e.getMessage() + "\n"); 
			System.out.println("Could not connect to the Zabbix Server at : " 
					+ Configuration.zabbixUrl + ". Exception: " 
					+ e.getMessage() + "\n");
		}
		return auth;
	}

	
	/**
	 * Gets all hosts in Zabbix server.
	 *
	 * @return List of all hosts
	 */
	public List<Host> getAllHosts(){		
		ArrayList<Host> hosts = null;
		
		try {
			String token = getAuth();
			if (token != null){
				String jsonRequest = 
						"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
						+ "\"params\":{\"output\":\"extend\"},"
						+ "\"method\":\"host.get\","
						+ "\"auth\":\""+ token + "\",\"id\": 0}";
						
				HttpResponse response = postAndGet(jsonRequest);
				HttpEntity entity = response.getEntity();
				ObjectMapper mapper = new ObjectMapper ();
				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
				ArrayList result = (ArrayList) untyped.get("result");

				if (result != null){
					hosts = new ArrayList<Host>();
					for (int i=0; i<result.size(); i++){
						Host host = Json2ObjectMapper.getHost((HashMap<String, String>) result.get(i));				
						hosts.add(host);
					}
				}
				return hosts;
			}

		} catch (Exception e) {
			log.error(e.getMessage() + "\n"); 
		}
		
		return hosts;
	}
	
	/**
	 * Gets a specific host by name.
	 *
	 * @param hostName the host name
	 * @return the host 
	 */
	public Host getHostByName(String hostName){
		Host host = null;
		
		try {
			String token = getAuth();
			
			if (token != null){
			
				String jsonRequest = 
						"{\"jsonrpc\":\""+ Dictionary.JSON_RPC_VERSION + "\","
						+ "\"params\":{\"output\":\"extend\","
									+ "\"filter\":{\"host\": [\"" + hostName + "\"]}"
									+ "},"
						+ "\"method\":\"host.get\",\"auth\":\"" + token + "\","
						+ "\"id\": 0}";
				
				HttpResponse response = postAndGet(jsonRequest);
				HttpEntity entity = response.getEntity();
				ObjectMapper mapper = new ObjectMapper ();
				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
				ArrayList result = (ArrayList) untyped.get("result");

				if (result != null){
					host = Json2ObjectMapper.getHost((HashMap<String, String>) result.get(0));					
					log.info("Host " + hostName + " finded in Zabbix");
					return host;
				}			
			}

		} catch (Exception e) {
			log.error(e.getMessage() + "\n"); 
		}
		return host;
	}
	
	/**
	 * Gets all items.
	 *
	 * @return List of items
	 */
	public List<Item> getAllItems(){
		ArrayList<Item> items = null;
		try {
			String token = getAuth();
			if (token != null){
				String jsonRequest = 
						"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
					   + "\"params\":{\"output\":\"extend\"},"
					   + "\"method\":\"item.get\","
					   + "\"auth\":\"" + token + "\","
					   + "\"id\":0}";
						
				HttpResponse response = postAndGet(jsonRequest);
				HttpEntity entity = response.getEntity();
				ObjectMapper mapper = new ObjectMapper ();
				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
				ArrayList result = (ArrayList) untyped.get("result");

				if (result != null){
					items = new ArrayList<Item>();
					for (int i=0; i<result.size(); i++){
						Item item = Json2ObjectMapper.getItem((HashMap<String, String>) result.get(i));				
						items.add(item);
					}
				}
				return items;
			}

		} catch (Exception e) {
			log.error(e.getMessage() + "\n"); 
		}
		
		return items;
	}
	
	/**
	 * Gets  all items available from host.
	 *
	 * @param hostName the host name
	 * @return the items from host
	 */
	public List<Item> getItemsFromHost(String hostName){
		ArrayList<Item> items = null;
		
		//Get info from host
		Host host = getHostByName(hostName);
		
		if (host != null){
			try {
				String token = getAuth();
				if (token != null){
					String jsonRequest = 
							"{\"jsonrpc\":\""+ Dictionary.JSON_RPC_VERSION + "\","
							+ "\"method\":\"item.get\","
							+ "\"params\":{\"output\":\"extend\","
										+ "\"hostids\":\""+ host.getHostid() +"\","
										+ "\"sortfield\":\"name\""
										+ "},"
							+ "\"auth\":\"" + token + "\","
							+ "\"id\":0}";

						HttpResponse response = postAndGet(jsonRequest);
						HttpEntity entity = response.getEntity();
						ObjectMapper mapper = new ObjectMapper ();
						HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
						ArrayList result = (ArrayList) untyped.get("result");

						if (result != null){
							items = new ArrayList<Item>();
							for (int i=0; i<result.size(); i++){
								Item item = Json2ObjectMapper.getItem((HashMap<String, String>) result.get(i));				
								items.add(item);
							}
						}
						return items;
				}
				
			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
			}
			
		}
		
		return items;
	}
		
	
	/**
	 * Gets a specific item by name from one host.
	 * 
	 * If there are more than one item, with this name, the method return the first item of the list
	 *
	 * @param itemName the item name
	 * @param hostName the host name
	 * @return the item from host
	 */
	public Item getItemByNameFromHost(String itemName, String hostName){
		Item item = null;
		
		//Get info from host
		Host host = getHostByName(hostName);

		if (host != null){
			try {
				String token = getAuth();
				if (token != null){
					String jsonRequest = 
							"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
						   + "\"method\":\"item.get\","
						   + "\"params\":{\"output\":\"extend\","
						   			   + "\"hostids\":\"" + host.getHostid() + "\","
						   			   + "\"search\":{\"name\":\"" + itemName + "\"},"
						   			   + "\"sortfield\":\"name\"},"
						   + "\"auth\":\"" + token + "\","
						   + "\"id\": 0}";

					HttpResponse response = postAndGet(jsonRequest);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper ();
					HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
					ArrayList result = (ArrayList) untyped.get("result");

					if (result != null){
						item = Json2ObjectMapper.getItem((HashMap<String, String>) result.get(0));				
					}
					return item;
				}

			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
			}

		}
		return item;
	}

	/**
	 * Post and get.
	 *
	 * @param request the request
	 * @return the http response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private HttpResponse postAndGet(String request) throws IOException {
	    HttpClient client = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost(Configuration.zabbixUrl);
	    httpPost.setEntity(new StringEntity(request));
	    httpPost.addHeader("Content-Type", "application/json-rpc");
	    return client.execute(httpPost);
	}

	
	/**
	 * Gets the history data from item.
	 *
	 * @param itemKey the item key
	 * @param hostName the host name
	 * @param itemFormat available format values:<BR>Dictionary.HISTORY_ITEM_FORMAT_FLOAT = float;<BR>
													Dictionary.HISTORY_ITEM_FORMAT_STRING = String<BR>
													Dictionary.HISTORY_ITEM_FORMAT_LOG = log<BR>
													Dictionary.HISTORY_ITEM_FORMAT_INTEGER = integer<BR>
													Dictionary.HISTORY_ITEM_FORMAT_TEXT = text<BR>
 	 * @param limit how many values are going to retrieve from Zabbix
	 * @return the history data from item
	 */
	public List<HistoryItem> getHistoryDataFromItem(String itemKey, String hostName, String itemFormat, int limit){
		ArrayList<HistoryItem> historyItems = null;
		
		if (limit > 0){
			//getHost
			Host host = getHostByName(hostName);
			if (host != null){
				//get itemId
				Item item = getItemByKeyFromHost(itemKey, hostName);
				if (item != null){
					try {
						String token = getAuth();
						if (token != null){
							//get historyData from host
							String jsonRequest = 
									"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
								   + "\"method\":\"history.get\","
								   + "\"params\":{\"output\":\"extend\","
								   			   + "\"history\": \"" + itemFormat + "\","
								   			   + "\"itemids\": \"" + item.getItemid() + "\","
								   			   + "\"hostids\": \"" + host.getHostid() + "\", "
								   			   + "\"sortfield\": \"clock\","
								   			   + "\"sortorder\": \"DESC\","
								   			   + "\"limit\": \"" + limit + "\"},"								   			
								   + "\"auth\":\"" + token + "\","
								   + "\"id\": 0}";

							HttpResponse response = postAndGet(jsonRequest);
							HttpEntity entity = response.getEntity();
							ObjectMapper mapper = new ObjectMapper ();
							HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
							ArrayList result = (ArrayList) untyped.get("result");

							System.out.println();
							
							if (result != null){
								historyItems = new ArrayList<HistoryItem>();
								for (int i=0; i<result.size(); i++){
									HistoryItem historyItem = Json2ObjectMapper.getHistoryItem((HashMap<String, Object>) result.get(i));
									historyItems.add(historyItem);
								}
							}
							return historyItems;
						}

					} catch (Exception e) {
						log.error(e.getMessage() + "\n"); 
						System.out.println(e.getMessage() + "\n");
					}					
				}
				else {
					log.error("No item with key = " + itemKey + ", available in host " + hostName); 
				}	
			}
			else {
				log.error("No host " + hostName + " available in Zabbix system");
			}				
		}
		else {
			log.error("limit must be greater than 0. Current value = " + limit);
		}
		
		return historyItems;
	}
	
	
	/**
	 * Gets a specific item by key from one host.
	 *
	 * @param itemKey the item key
	 * @param hostName the host name
	 * @return the item from host
	 */
	public Item getItemByKeyFromHost(String itemKey, String hostName){
		Item item = null;
		
		//Get info from host
		Host host = getHostByName(hostName);

		if (host != null){
			try {
				String token = getAuth();
				if (token != null){
					String jsonRequest = 
							"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
						   + "\"method\":\"item.get\","
						   + "\"params\":{\"output\":\"extend\","
						   			   + "\"hostids\":\"" + host.getHostid() + "\","
						   			   + "\"search\":{\"key_\":\"" + itemKey + "\"},"
						   			   + "\"sortfield\":\"name\"},"
						   + "\"auth\":\"" + token + "\","
						   + "\"id\": 0}";

					HttpResponse response = postAndGet(jsonRequest);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper ();
					HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
					ArrayList result = (ArrayList) untyped.get("result");

					if (result != null){
						item = Json2ObjectMapper.getItem((HashMap<String, String>) result.get(0));				
					}
					return item;
				}

			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
			}

		}
		return item;
	}
	
	/**
	 * Gets the history data from item.
	 *
	 * @param itemKey the item key
	 * @param hostName the host name
	 * @param itemFormat available format values:<BR>Dictionary.HISTORY_ITEM_FORMAT_FLOAT = float;<BR>
	 * 													Dictionary.HISTORY_ITEM_FORMAT_STRING = String<BR>
	 * 													Dictionary.HISTORY_ITEM_FORMAT_LOG = log<BR>
	 * 													Dictionary.HISTORY_ITEM_FORMAT_INTEGER = integer<BR>
	 * 													Dictionary.HISTORY_ITEM_FORMAT_TEXT = text<BR>
	 * @param startTime the start time in miliseconds
	 * @param endTime the end time in miliseconds
	 * @return the history data from item
	 */
	public List<HistoryItem> getHistoryDataFromItem(String itemKey, String hostName, String itemFormat, long startTime,
			long endTime){
		ArrayList<HistoryItem> historyItems = null;
		
		if (startTime >= endTime){
			Date date = new Date();
			long actualDate = date.getTime();
			if (!(startTime > actualDate) && !(endTime > actualDate)){
				//getHost
				Host host = getHostByName(hostName);
				if (host != null){
					//get itemId
					Item item = getItemByKeyFromHost(itemKey, hostName);
					if (item != null){
						try {
							String token = getAuth();
							if (token != null){
								//get historyData from host
								String jsonRequest = 
										"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
									   + "\"method\":\"history.get\","
									   + "\"params\":{\"output\":\"extend\","
									   			   + "\"history\": \"" + itemFormat + "\","
									   			   + "\"itemids\": \"" + item.getItemid() + "\","
									   			   + "\"hostids\": \"" + host.getHostid() + "\", "
									   			   + "\"time_from\": \"" + startTime + "\", "
									   			   + "\"time_till\": \"" + endTime + "\", "
									   			   + "\"sortfield\": \"clock\","
									   			   + "\"sortorder\": \"DESC\"},"								   			
									   + "\"auth\":\"" + token + "\","
									   + "\"id\": 0}";

								HttpResponse response = postAndGet(jsonRequest);
								HttpEntity entity = response.getEntity();
								ObjectMapper mapper = new ObjectMapper ();
								HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
								ArrayList result = (ArrayList) untyped.get("result");

								System.out.println();
								
								if (result != null){
									historyItems = new ArrayList<HistoryItem>();
									for (int i=0; i<result.size(); i++){
										HistoryItem historyItem = Json2ObjectMapper.getHistoryItem((HashMap<String, Object>) result.get(i));
										historyItems.add(historyItem);
									}
								}
								return historyItems;
							}

						} catch (Exception e) {
							log.error(e.getMessage() + "\n"); 
							System.out.println(e.getMessage() + "\n");
						}					
					}
					else {
						log.error("No item with key = " + itemKey + ", available in host " + hostName); 
					}	
				}
				else {
					log.error("No host " + hostName + " available in Zabbix system");
				}
			}							
		}
		else {
			log.error("endTime must be greater than startTime: startTime = " + startTime + ", endTime = " + endTime);
		}
		
		return historyItems;
		
		
	}
	
	
}
