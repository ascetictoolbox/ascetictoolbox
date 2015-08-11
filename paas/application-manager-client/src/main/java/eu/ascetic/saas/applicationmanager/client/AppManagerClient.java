package eu.ascetic.saas.applicationmanager.client;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;



// TODO: Auto-generated Javadoc
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
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * The Interface VmManagerClient.
 */

public interface AppManagerClient {
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getURL();

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setURL(String url);
	

	/**
	 * Adds the application.
	 *
	 * @return the string
	 */
	public Application createApplication(String ovf);
	
	public Collection getApplications();
	
	public Application getApplication(String appId);
	
	public Collection getDeployments(String appId);
	
	public Deployment getDeployment(String appId, String deploymentId);
	
	public Agreement getAgreement(String appId, String deploymentId);
	
	public Deployment createDeployment(String appId, String ovf);
	
	//public void deleteApplication(String appId);
	//public void deleteDeployment(String appId, String deploymentId);
	//public Agreement acceptAgreement(String appId, String deploymentId, String agreementId); //reject??
	
}
