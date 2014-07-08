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

import es.bsc.servicess.ide.Logger;
import es.bsc.servicess.ide.views.DeploymentChecker;
import eu.ascetic.saas.application_uploader.ApplicationUploader;

public class AsceticDeploymentChecker implements DeploymentChecker {
	private ApplicationUploader AMClient;
	private String applicationID;
	private static Logger log = Logger.getLogger(AsceticDeploymentChecker.class);
	
	public AsceticDeploymentChecker(ApplicationUploader appUploader, String applicationID) {
		this.AMClient = appUploader;
		
	}

	@Override
	public String getStatus(String deploymentID) {
		try {
			return AMClient.getDeploymentStatus(applicationID, deploymentID);
			//return "Deployed";
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Map<String, Map<String, String>> getMachines(String deploymentID) {
		try {
			return AMClient.getDeployedVMs(applicationID, deploymentID);
		} catch (Exception e) {
			log.error("Error getting machines from application manager", e);
			return null;
		}
	
	}


	@Override
	public void undeploy(String serviceID, boolean keepData) {
		try {
			AMClient.undeploy(serviceID, applicationID);
		}catch (Exception e) {
			log.error("Error undeploying application", e);
		}
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
