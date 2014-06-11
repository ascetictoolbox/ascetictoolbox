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
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import eu.ascetic.monitoring.api.datamodel.Host;
import eu.ascetic.monitoring.api.datamodel.Item;
import eu.ascetic.monitoring.api.datamodel.User;
import eu.ascetic.monitoring.api.utils.Dictionary;
import eu.ascetic.monitoring.api.utils.Json2ObjectMapper;

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
	 * Instantiates a new zabbix manager.
	 *
	 * @param login the login
	 * @param password the password
	 */
	public ZabbixClient(String login, String password){
		user = new User(login,password);		
	}
		
	/**
	 * Gets the auth.
	 *
	 * @param user the user
	 * @param password the password
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
						+ Dictionary.ZABBIX_SERVER_URL + ", using login: "
						+ user.getLogin());
			}
			log.info("Successfully connected to the server\n");

		} catch (IOException e) {
			log.error("Could not connect to the Zabbix Server at : " 
					+ Dictionary.ZABBIX_SERVER_URL + ". Exception: " 
					+ e.getMessage() + "\n"); 
		}
		return auth;
	}

	
	/**
	 * Gets the all hosts.
	 *
	 * @return the all hosts
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
	 * Gets the host by name.
	 *
	 * @param hostName the host name
	 * @return the host by name
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
	 * Gets the all items.
	 *
	 * @param u the u
	 * @return the all items
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
	 * Gets the items from host.
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
	 * Gets the item from host.
	 *
	 * @param itemName the item name
	 * @param hostName the host name
	 * @return the item from host
	 */
	public Item getItemFromHost(String itemName, String hostName){
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
	    HttpPost httpPost = new HttpPost(Dictionary.ZABBIX_SERVER_URL);
	    httpPost.setEntity(new StringEntity(request));
	    httpPost.addHeader("Content-Type", "application/json-rpc");
	    return client.execute(httpPost);
	}
	
}
