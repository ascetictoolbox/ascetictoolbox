package eu.ascetic.asceticarchitecture.iaas.zabbixApi.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.conf.Configuration;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HistoryItem;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.HostGroup;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Template;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.User;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Json2ObjectMapper;

/**
 * /**
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
 * Java representation of Zabbix client
 * 
 */
public class ZabbixClient {

	/** The log. */
	private Logger log = Logger.getLogger(this.getClass().getName());	
	
	/** The user. */
	private User user;
        
	/** The http client used to contact Zabbix */
	private final HttpClient client = HttpClientBuilder.create().build();

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
		String auth = user.getAuth();
                if (auth != null) {
                    GregorianCalendar now = new GregorianCalendar();
                    long expiryTime = TimeUnit.MILLISECONDS.toSeconds(user.getAuthExpiryDate().getTimeInMillis());
                    long currentTime = TimeUnit.MILLISECONDS.toSeconds(now.getTimeInMillis());
                    expiryTime = expiryTime - TimeUnit.MINUTES.toSeconds(15); //add a safety margin
                    if (currentTime < expiryTime) { //return the cached version!!
                        return auth;
                    } else {
                        auth = null; //delete the cache and try again
                        user.setAuth(null);
                        user.setAuthExpiryDate(null);
                    }
        }

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
                GregorianCalendar expiryDate = new GregorianCalendar();
                long expiaryTime = TimeUnit.SECONDS.toMillis(990); //default expiary time
                expiryDate.setTimeInMillis(expiryDate.getTimeInMillis() + expiaryTime);
                user.setAuthExpiryDate(expiryDate);
                user.setAuth(auth);
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
    public Host getHostByName(String hostName) {
        Host host = null;
		
        try {
            String token = getAuth();
			
            if (token != null) {

				String jsonRequest = 
						"{\"jsonrpc\":\""+ Dictionary.JSON_RPC_VERSION + "\","
                        + "\"params\":{\"output\":\"extend\","
                        + "\"filter\":{\"host\": [\"" + hostName + "\"]}"
                        + "},"
                        + "\"method\":\"host.get\",\"auth\":\"" + token + "\","
                        + "\"id\": 0}";

                HttpResponse response = postAndGet(jsonRequest);
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = new ObjectMapper();
                HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
                ArrayList result = (ArrayList) untyped.get("result");

                if (result != null && !result.isEmpty()) {
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
     * @param itemFormat available format
     * values:<BR>Dictionary.HISTORY_ITEM_FORMAT_FLOAT = float;<BR>
     * Dictionary.HISTORY_ITEM_FORMAT_STRING = String<BR>
     * Dictionary.HISTORY_ITEM_FORMAT_LOG = log<BR>
     * Dictionary.HISTORY_ITEM_FORMAT_INTEGER = integer<BR>
     * Dictionary.HISTORY_ITEM_FORMAT_TEXT = text<BR>
     * @param startTime the start time in milliseconds
     * @param endTime the end time in milliseconds
     * @return the history data from item
     */
    public List<HistoryItem> getHistoryDataFromItem(String itemKey, String hostName, String itemFormat, long startTime,
            long endTime) {
        ArrayList<HistoryItem> historyItems = null;

        if (startTime <= endTime) {
            Date date = new Date();
            long actualDate = date.getTime();
            if (!(startTime > actualDate) && !(endTime > actualDate)) {
                //getHost
                Host host = getHostByName(hostName);
                if (host != null) {
                    //get itemId
                    Item item = getItemByKeyFromHost(itemKey, hostName);
                    if (item != null) {
                        try {
                            String token = getAuth();
                            if (token != null) {
                                //get historyData from host
                                String jsonRequest
                                        = "{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
                                        + "\"method\":\"history.get\","
                                        + "\"params\":{\"output\":\"extend\","
                                        + "\"history\": \"" + itemFormat + "\","
                                        + "\"itemids\": \"" + item.getItemid() + "\","
                                        + "\"hostids\": \"" + host.getHostid() + "\", "
                                        + "\"time_from\": \"" + TimeUnit.MILLISECONDS.toSeconds(startTime) + "\", "
                                        + "\"time_till\": \"" + TimeUnit.MILLISECONDS.toSeconds(endTime) + "\", "
                                        + "\"sortfield\": \"clock\","
                                        + "\"sortorder\": \"DESC\"},"
                                        + "\"auth\":\"" + token + "\","
                                        + "\"id\": 0}";

                                HttpResponse response = postAndGet(jsonRequest);
                                HttpEntity entity = response.getEntity();
                                ObjectMapper mapper = new ObjectMapper();
                                HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
                                ArrayList result = (ArrayList) untyped.get("result");

                                System.out.println();

                                if (result != null) {
                                    historyItems = new ArrayList<HistoryItem>();
                                    for (int i = 0; i < result.size(); i++) {
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
                    } else {
                        log.error("No item with key = " + itemKey + ", available in host " + hostName);
                    }
                } else {
                    log.error("No host " + hostName + " available in Zabbix system");
                }
            }
        } else {
            log.error("endTime must be greater than startTime: startTime = " + startTime + ", endTime = " + endTime);
        }

        return historyItems;
    }
	
	/**
	 * Creates a new VM in Zabbix.
	 *
	 * @param hostName the host name
	 * @param ipAddress the ip address
	 * @return the id of the new VM created in Zabbix
	 */
	public String createVM(String hostName, String ipAddress){
		String newHostId = null;

		if (getHostByName(hostName) == null){
			//hostGroup
			HostGroup vmsHostGroup = getHostGroupByName(Configuration.virtualMachinesGroupName);
			if (vmsHostGroup !=null){
				//templates
				Template linuxTemplate = getTemplateByName(Configuration.osLinuxTemplateName);
				if (linuxTemplate != null){
					//With all data validated, we create the new VM
					newHostId = createVM(hostName,ipAddress,vmsHostGroup,linuxTemplate);
				}
				else {
					log.error("The template " + Configuration.osLinuxTemplateName + " is not available in Zabbix");
					return null;
				}						
			}
			else {
				log.error("The hostGroupName is null or empty");
				return null;
			}

		}
		else {
			log.error("The host " + hostName + " already exists in the ASCETiC Zabbix environment. Please choose another hostname" );
			return null;
		}
		
		return newHostId;
	}
	
	
	/**
	 * Creates the vm.
	 *
	 * @param hostName the host name
	 * @param ipAddress the ip address
	 * @param vmsHostGroup the vms host group
	 * @param vmTemplate the vm template
	 * @return the id of the new VM created
	 */
	private String createVM(String hostName, String ipAddress, HostGroup vmsHostGroup, Template vmTemplate) {
		String newHostId = null;
		try {

			String token = getAuth();
			if (token != null){
				//get historyData from host
				String jsonRequest = 	
						"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
								+ "\"method\": \"host.create\","
								+ "\"params\": {"
								+ "\"host\": \"" + hostName + "\","
								+ "\"interfaces\": ["
								+ "{"
								+ "\"type\": 1,"
								+ "\"main\": 1,"
								+ "\"useip\": 1,"
								+ "\"ip\": \"" + ipAddress +"\","
								+ "\"dns\": \"\","
								+ "\"port\": \"10050\""
								+ "}"
								+ "],"
								+ "\"groups\": ["
								+ "{\"groupid\": \"" + vmsHostGroup.getGroupId() + "\"}"
								+ "],"
								+ "\"templates\": ["
								+ "{\"templateid\": \"" + vmTemplate.getTemplateId() + "\"}"
								+ "]"
								+ "},"
								+ "\"auth\":\"" + token + "\","
								+ "\"id\": 1"
								+ "}";

				HttpResponse response = postAndGet(jsonRequest);
				HttpEntity entity = response.getEntity();
				ObjectMapper mapper = new ObjectMapper ();
				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
				LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) untyped.get("result");

				if (result != null){
					ArrayList<String> list  = (ArrayList<String>) result.get("hostids");
					newHostId = list.get(0);					
					log.info("New VM created in Zabbix. HostID = " + newHostId);
					return newHostId;
				}	
			}

		} catch (Exception e) {
			log.error(e.getMessage() + "\n"); 
			System.out.println(e.getMessage() + "\n");
			return null;
		}
		return newHostId;
	}

	/**
	 * Gets the host group by name.
	 *
	 * @param hostGroupName the host group name
	 * @return the host group by name
	 */
	private HostGroup getHostGroupByName(String hostGroupName){
		HostGroup hostGroup = null;
		if (hostGroupName!=null && !hostGroupName.isEmpty()){
			
			try {
				String token = getAuth();
				if (token != null){
					//get historyData from host
					String jsonRequest = 							
						"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\"," 
						    + "\"method\": \"hostgroup.get\","
						    + "\"params\": {"
						    	+ "\"output\": \"extend\","
						        + "\"filter\": {"
						            + "\"name\": [\"" + hostGroupName + "\"]"
						        + "}"
						    + "}," 
						    + "\"auth\":\"" + token + "\","
						    + "\"id\": 0}";

					HttpResponse response = postAndGet(jsonRequest);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper ();
					HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
					ArrayList result = (ArrayList) untyped.get("result");

					if (result != null){
						hostGroup = Json2ObjectMapper.getHostGroup((HashMap<String, String>) result.get(0));					
						log.info("HostGroup " + hostGroupName + " finded in Zabbix");
						return hostGroup;
					}	
				}

			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
				System.out.println(e.getMessage() + "\n");
			}							
		}
		else {
			log.error("The hostGroupName is null or empty");
		}
		return hostGroup;
	}
	
	
	/**
	 * Gets the template by name.
	 *
	 * @param templateName the template name
	 * @return the template by name
	 */
	public Template getTemplateByName(String templateName){
		Template template = null;
		if (templateName!=null && !templateName.isEmpty()){
			
			try {
				String token = getAuth();
				if (token != null){
					String jsonRequest = 	
							"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\"," 
							+ "\"method\":\"template.get\"," 
							+ "\"params\":{\"output\":\"extend\","
									+ "\"filter\":{\"name\": [\"" + templateName + "\"]}"
								+ "},"
							+ "\"auth\":\"" + token + "\","
							+ "\"id\": 0}";							
						
					HttpResponse response = postAndGet(jsonRequest);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper ();
					HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
					ArrayList result = (ArrayList) untyped.get("result");

					if (result != null){
						template = Json2ObjectMapper.getTemplate((HashMap<String, String>) result.get(0));					
						log.info("Template " + templateName + " finded in Zabbix");
						return template;
					}	
				}

			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
				System.out.println(e.getMessage() + "\n");
			}							
		}
		else {
			log.error("The templateName is null or empty");
		}
		return template;
	}
	
	
	
	/**
	 * Delete vm.
	 *
	 * @param hostName the host name
	 * @return the ID of the VM deleted
	 */
	public String deleteVM(String hostName){
		String hostDeletedId = null;
		Host hostToDelete = getHostByName(hostName);
		if ( hostToDelete != null){
			//With all data validated, we create the new VM
			try {
				String token = getAuth();
				if (token != null){
					String jsonRequest = 							
							"{\"jsonrpc\":\"" + Dictionary.JSON_RPC_VERSION + "\","
									+ "\"method\":\"host.delete\","
									+ "\"params\":[\"" + hostToDelete.getHostid() + "\"],"
									+ "\"auth\":\"" + token + "\","
									+ "\"id\": 1"
									+ "}";
							
					HttpResponse response = postAndGet(jsonRequest);
					HttpEntity entity = response.getEntity();
					ObjectMapper mapper = new ObjectMapper ();
					HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
					LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) untyped.get("result");

					if (result != null){
						ArrayList<String> list  = (ArrayList<String>) result.get("hostids");
						hostDeletedId = list.get(0);					
						log.info("VM deleted in Zabbix. HostID = " + hostDeletedId);
						return hostDeletedId;
					}	
				}

			} catch (Exception e) {
				log.error(e.getMessage() + "\n"); 
				System.out.println(e.getMessage() + "\n");
				return null;
			}
		}
		else {
			log.error("The VM " + hostName + " not exists in the ASCETiC Zabbix environment. Please check the name" );
			return null;
		}
		
		return hostDeletedId;
	}
	
	
	
	/**
	 * Push data.
	 *
	 * @param hostName the host name
	 * @param itemKey the item key
	 * @param value the value
	 * @return the string
	 */
	private boolean pushData(String hostName, String itemKey, String value){
		boolean sent = false;
//
//		
//		if (getHostByName(hostName) != null){
//			try {
//				String jsonRequest = 							
//						"{\"request\":\"sender data\",\"data\":[{" + 
//								"\"host\":\"" + hostName + "\"," +
//						        "\"key\":\"" + itemKey + "\"," +
//						        "\"value\":\"" + value + "\"}]}";
//				
//				HttpResponse response = postAndGet(jsonRequest);
//				HttpEntity entity = response.getEntity();
//				ObjectMapper mapper = new ObjectMapper ();
//				HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
//	//			LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>) untyped.get("result");
//				String result = (String) untyped.get("info");
//	
//				if (result != null){		
//					log.info("Data sent to zabbix. Result:  " + result);
//					System.out.println("Data sent to zabbix. Result:  " + result);
//					return true;
//				}	
//			} catch (Exception e) {
//				log.error(e.getMessage() + "\n"); 
//				System.out.println(e.getMessage() + "\n");
//				return false;
//			}
//		}
//		else {
//			log.error("The host " + hostName + " doesn't exists in the ASCETiC Zabbix environment. Please choose another hostname" );
//			return false;
//		}
		
		return sent;
	}
}
