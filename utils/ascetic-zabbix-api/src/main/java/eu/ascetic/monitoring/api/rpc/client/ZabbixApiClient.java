package eu.ascetic.monitoring.api.rpc.client;

//The Client sessions package
//For creating URLs
import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


//The JSON Smart package for JSON encoding/decoding (optional)

//curl -i -X POST -H 'Content-Type: application/json-rpc' -d '
//{"params": {"password": "zabbix", "user": "Admin"}, "jsonrpc": "2.0", "method": "user.authenticate", "auth": "", "id": 0}' http://172.24.76.63/zabbix/api_jsonrpc.php


/**
 * The Class ZabbixApiClient.
 */
public class ZabbixApiClient {

	/** The zabbix server url. */
	private static String zabbixServerUrl = "http://172.24.76.27/zabbix/api_jsonrpc.php";
	
	private static String auth;
	
	/**
	 * Instantiates a new zabbix api client.
	 *
	 * @param serverUrl the server url
	 */
	public ZabbixApiClient(String serverUrl){
		zabbixServerUrl = serverUrl;			
	}
	

	
	
	private static HashMap<String,Object> createHostCreateParams(String auth){
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("host", "Linux server");
		
		HashMap<String, Object> interfaces = new HashMap<String, Object>();
		interfaces.put("type", 1);
		interfaces.put("main", 1);
		interfaces.put("useip", 1);
		interfaces.put("ip","192.168.3.1");
		interfaces.put("dns", "");
		interfaces.put("port", "10050");
		params.put("interfaces", interfaces);
		
		HashMap<String, Object> groups = new HashMap<String, Object>();
		groups.put("groupid", "50");
		params.put("groups", groups);
		
		HashMap<String, Object> templates = new HashMap<String, Object>();
		templates.put("templateid", "20045");
		params.put("templates", templates);

		HashMap<String, Object> inventory = new HashMap<String, Object>();
		inventory.put("macaddress_a", "01234");
		inventory.put("macaddress_b", "56768");
		params.put("inventory", inventory);

		params.put("auth", auth);
		params.put("id", 1);
		return params;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
//		try {
			//clientTest();
			String token = authenticate("Admin","zabbix",zabbixServerUrl);
			System.out.println(token);
			createHost(token);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//		catch (JSONRPC2SessionException e) {
//			e.printStackTrace();
//		}

	}
	
	public static String authenticate(String username, String password, String url) {
//	    this.password = password;
//	    this.userName = username;
//	    zabbixApiUrl = url;
		
		
	    StringBuilder uiConnectMessage = new StringBuilder();
	    StringBuilder sb = new StringBuilder();
	    sb.append("{\"jsonrpc\":\"2.0\"").
	    append(",\"params\":{").
	    append("\"user\":\"").append(username).
	    append("\",\"password\":\"").append(password).
	    append("\"},").
	    append("\"method\":\"user.authenticate\",").
	    append("\"id\":\"2\"}");
	 
	    try {
	        HttpResponse response = postAndGet(sb.toString());
	        HttpEntity entity = response.getEntity();
	 
	        ObjectMapper mapper = new ObjectMapper ();
			HashMap untyped = mapper.readValue(EntityUtils.toString(entity), HashMap.class);
	        auth = (String) untyped.get("result");
	 
	        if (auth == null) {
	            throw new IllegalArgumentException("Authorization failed to : " + url + ", using username : "               + username);
	        }
	        System.out.println("auth: " + auth);
	        uiConnectMessage.append("Successfully connected to the server\n");
	 
	    } catch (IOException e) {
	        uiConnectMessage.append("Could not connect to the Zabbix Server at : ").
	        append(url).append(" Exception : ").append(e.getMessage()).append("\n");
	    }
	   // return uiConnectMessage.toString();
	    return auth;
	}
	
	
	public static void createHost(String token){
		HashMap map = createHostCreateParams(token);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		String json2 = "";
		try {
			json = mapper.writeValueAsString(map);
			json2 = mapper.defaultPrettyPrintingWriter().writeValueAsString(map);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("*********************************************************************************************");
		System.out.println(json);
		System.out.println("*********************************************************************************************");
		System.out.println("*********************************************************************************************");
		System.out.println(json2);
		System.out.println("*********************************************************************************************");
		
	}
	
	private static HttpResponse postAndGet(String request) throws IOException {
	    HttpClient client = new DefaultHttpClient();
	    HttpPost httpPost = new HttpPost(zabbixServerUrl);
	    httpPost.setEntity(new StringEntity(request));
	    httpPost.addHeader("Content-Type", "application/json-rpc");
	    return client.execute(httpPost);
	}

}
