package eu.ascetic.saas.applicationpackager.appmanager.client;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs.AppManagerCallProgressBarDialog;



// TODO: Auto-generated Javadoc
/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
	 * @param ovf the ovf
	 * @param dialog the dialog
	 * @return the string
	 */
	public Application postApplication(String ovf, AppManagerCallProgressBarDialog dialog);
	
	/**
	 * Gets the deployment.
	 *
	 * @param appId the app id
	 * @param deploymentId the deployment id
	 * @return the deployment
	 */
	public Deployment getDeployment(String appId, String deploymentId);
	
	
}
