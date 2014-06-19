/*
 *  Copyright 2011-2012 Barcelona Supercomputing Center (www.bsc.es)
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



import java.util.Map;

import es.bsc.servicess.ide.views.DeploymentChecker;

public class AsceticDeploymentChecker implements DeploymentChecker {
	private String AM_endpoint;
	private ApplicationManagerClient AMClient;

	public AsceticDeploymentChecker(String AM_endpoint) {
		this.AM_endpoint = AM_endpoint;
		this.AMClient = new ApplicationManagerClient(AM_endpoint);
	}

	@Override
	public String getStatus(String serviceID) {
		try {
			return AMClient.getStatus(serviceID);
			//return "Deployed";
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Map<String, Map<String, String>> getMachines(String serviceID) {
		try {
			
			return AMClient.getIPAddresses(serviceID);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error getting machines from service manager");
			return null;
		}
	
	}


	@Override
	public void undeploy(String serviceID, boolean keepData) {
		AMClient.undeployService(serviceID, keepData);
		
	}

	@Override
	public void stop(String serviceID) {
		// TODO implement stop
		
	}

	@Override
	public void start(String serviceID) {
		// TODO implement start
		
	}

	
	

}
