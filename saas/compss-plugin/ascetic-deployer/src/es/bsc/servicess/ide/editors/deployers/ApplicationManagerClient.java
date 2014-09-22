/*
 *  Copyright 2013-2014 Barcelona Supercomputing Center (www.bsc.es)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package es.bsc.servicess.ide.editors.deployers;

import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


public class ApplicationManagerClient {
	private WebResource applicationManager;

	public ApplicationManagerClient(String location) {
		Client c = Client.create();
		applicationManager = c.resource(location);
	}

	public String getStatus(String serviceID) throws Exception {
		//TODO implement get status
		String xml = applicationManager.path(serviceID).get(String.class);
		
		return null;
	}


	public Map<String, Map<String, String>> getIPAddresses(String serviceID)
			throws Exception {
		
		Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
		String xml = applicationManager.path(serviceID).get(String.class);
		//TODO implement get ip addresses

		return map;
	}

	
	
	public void undeployService(String serviceID, boolean keepData){
		//TODO implement undeploy Service
		applicationManager.path(serviceID).path("undeploy").post(Boolean.toString(keepData));
	}
	

	public String deployService(String ovf){
		//TODO implement deploy Service
		return null;
	}
}
