/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */

package eu.ascetic.paas.slam.poc.impl.paasapi;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.paas.slam.poc.impl.config.ConfigManager;


public class PaaSApiManager {

	private static WebResource webResource;
	
	private static PaaSApiManager instance = null;

	public static PaaSApiManager getInstance() {
		if (instance == null) {
			synchronized (PaaSApiManager.class) {
				instance = new PaaSApiManager();
			}
		}
		return instance;
	}

	
	private PaaSApiManager() {

		ConfigManager cm = ConfigManager.getInstance();
		
		String url = "http://" + cm.getFedApiHost() + ":" + 
				cm.getFedApiPort() + "/" + cm.getFedApiBasePath();
		try {
			Client client = new Client();
			webResource = client.resource(url);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Could not instantiate JSON client");
		}
	}
	
	
	
	public Boolean setMinimumLoa(int loaValue, String userUuid, String appUuid) {
		
		String pathUserAppl = "users/" + userUuid + "/applications/" + appUuid;
		String pathAttributes = "attributes";
		String minLoaAttrName = null;

		try {
			
			ClientResponse respGet = webResource.path(pathAttributes).
					type(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
			
			String res = respGet.getEntity(String.class);
			LOGGER.debug("Attributes: " + res);
			
			JSONArray jsonResp = new JSONArray(res);
			
			for (int i = 0; i < jsonResp.length(); i++) {
				JSONObject o = jsonResp.getJSONObject(i);
				if ("urn:contrail:names:provider:subject:minimum-loa".equals(o.getString("name"))) {
					minLoaAttrName = o.getString("uri");
					LOGGER.debug("Mininum LoA attribute URI: " + minLoaAttrName);
					break;
				}
			}
			
			if (minLoaAttrName == null) {
				LOGGER.error("Cannot get mininum LoA attribute URI");
				return false;
			}
			
			JSONObject attributes = new JSONObject();
			attributes.put(minLoaAttrName, loaValue);
			JSONObject json = new JSONObject();
			json.accumulate("attributes", attributes);
			
			ClientResponse respPut = webResource.path(pathUserAppl).
					type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, json.toString());
	
			LOGGER.debug("PUT call response status: " + respPut.getClientResponseStatus().toString());
			
			} catch (JSONException e) {
				e.printStackTrace();
				return false;

			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} 
		
		return true;
	}

	
	
	private static final Logger LOGGER = Logger.getLogger(PaaSApiManager.class);
}
