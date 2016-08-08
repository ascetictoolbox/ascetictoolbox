package eu.ascetic.saas.applicationpackager.appmanager.client;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
//import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.saas.applicationpackager.Dictionary;
import eu.ascetic.saas.applicationpackager.conf.Configuration;
import eu.ascetic.saas.applicationpackager.http.Client;
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
 * @author David Rojo, David Garcia Perez Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net , david.garciaperez@atos.net
 * 
 * The Apache HTTP Client implementation of the VM Manager ASCETiC Client
 */
public class AppManagerClientHC implements AppManagerClient {
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(AppManagerClientHC.class);
	
	/** The url. */
	private String url;
	
	/**
	 * Creates a new VM Manager Client using the URL
	 * to the VM Manager the one in the Application Manager
	 * Configuration file.
	 */
	public AppManagerClientHC() {
		this.url = Configuration.applicationManagerUrl;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.saas.applicationpackager.appmanager.client.AppManagerClient#getURL()
	 */
	@Override
	public String getURL() {
		return url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.saas.applicationpackager.appmanager.client.AppManagerClient#setURL(java.lang.String)
	 */
	@Override
	public void setURL(String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.saas.applicationpackager.appmanager.client.AppManagerClient#postApplication(java.lang.String)
	 */
	@Override
	public Application postApplication(String ovf, AppManagerCallProgressBarDialog dialog) {
		
		if (dialog != null){
			dialog.updateProgressBar(1);
		}
		Application app  = null;
		Boolean exception = false;
		String vmDeployUrl = url + "/applications";
		logger.info("URL build: " + vmDeployUrl);
		if (dialog != null){
			dialog.addLogMessage("URL build: " + vmDeployUrl);
		}
		
		try {
//			ListVms listVms = new ListVms(vms);
//			String payload = ModelConverter.objectListVmsToJSON(listVms);
//			
			String response = Client.postMethod(vmDeployUrl, ovf, Dictionary.CONTENT_TYPE_XML, Dictionary.CONTENT_TYPE_XML, exception, dialog);
			logger.info("PAYLOAD: " + response);
			if (dialog != null){
				dialog.addLogMessage("PAYLOAD: " + response);
			}
			app = ModelConverter.xmlApplicationToObject(response);
			
//			assertEquals("/applications/101", application.getHref());
//			assertEquals(101, application.getId());
//			assertEquals("Name", application.getName());
//			assertEquals(2, application.getDeployments().size());
//			assertEquals("/applications/101/deployments/1", application.getDeployments().get(0).getHref());
//			assertEquals(1, application.getDeployments().get(0).getId());
//			assertEquals("/applications/101/deployments/2", application.getDeployments().get(1).getHref());
//			assertEquals(2, application.getDeployments().get(1).getId());
//			assertEquals(2, application.getLinks().size());
//			assertEquals("parent", application.getLinks().get(0).getRel());
//			assertEquals("/", application.getLinks().get(0).getHref());
//			assertEquals("application/xml", application.getLinks().get(0).getType());
//			assertEquals("self", application.getLinks().get(1).getRel());
//			assertEquals("/101", application.getLinks().get(1).getHref());
//			assertEquals("application/xml", application.getLinks().get(1).getType());
			
//			
//			try {
//				listIDs = ModelConverter.jsonListStringToObject(response);
//			} catch(Exception e) {
//				logger.warn("Error trying incoming list of new IDs to object. Exception: " + e.getMessage());
//				exception = true;
//			}
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + url + "/vms" + " Exception: " + e.getMessage());
			if (dialog != null){
				dialog.addLogMessage("Error trying to parse XML response from AppMan: " + url + "/vms" + " Exception: " + e.getMessage());
			}
			exception = true;
		}
		
		if(exception) return null;
//		return listIDs;
		return app;
	}

	
	public Application postApplication(String ovf) {
		
		
		Application app  = null;
		Boolean exception = false;
		String vmDeployUrl = url + "/applications";
		logger.info("URL build: " + vmDeployUrl);
		
		try {
//			ListVms listVms = new ListVms(vms);
//			String payload = ModelConverter.objectListVmsToJSON(listVms);
//			
			String response = Client.postMethod(vmDeployUrl, ovf, Dictionary.CONTENT_TYPE_XML, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			app = ModelConverter.xmlApplicationToObject(response);
			
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + url + "/vms" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return app;
	}
	
	public void showDeploymentURL(String appName, String deploymentId){
		String targetUrl = url + "/applications/" + appName + "/deployments/" + deploymentId;
		logger.info("Connecting to: " + targetUrl);		
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.saas.applicationpackager.appmanager.client.AppManagerClient#getDeployment(java.lang.String, java.lang.String)
	 */
	@Override
	public Deployment getDeployment(String appName, String deploymentId) {
		Deployment deployment  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications/" + appName + "/deployments/" + deploymentId;
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
//			logger.info("PAYLOAD: " + response);
			deployment = ModelConverter.xmlDeploymentToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return deployment;
	}
	
	

	
}
