package eu.ascetic.saas.applicationmanager.client;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.saas.applicationmanager.client.conf.Configuration;
import eu.ascetic.saas.applicationmanager.client.http.Client;
import eu.ascetic.saas.applicationmanager.client.utils.Dictionary;


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
 * @author David Rojo, David Garcia Perez Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net , david.garciaperez@atos.net
 * 
 * The Apache HTTP Client implementation of the VM Manager ASCETiC Client
 */
public class AppManagerClientHC implements AppManagerClient {
	private static Logger logger = Logger.getLogger(AppManagerClientHC.class);
	
	private String url;
	
	/**
	 * Creates a new VM Manager Client using the URL
	 * to the VM Manager the one in the Application Manager
	 * Configuration file.
	 */
	public AppManagerClientHC() {
		this.url = Configuration.applicationManagerUrl;
	}

	@Override
	public String getURL() {
		return url;
	}

	@Override
	public void setURL(String url) {
		this.url = url;
	}

	@Override
	public Application createApplication(String ovf) {
		
		Application app  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications";
		logger.info("URL build: " + targetUrl);
		
		try {

			String response = Client.postMethod(targetUrl, ovf, Dictionary.CONTENT_TYPE_XML, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
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
			
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from Application Manager: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
//		return listIDs;
		return app;
	}

	
	@Override
	public Collection getApplications() {
		Collection collection  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications";
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			collection = ModelConverter.xmlCollectionToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return collection;
	}

	@Override
	public Application getApplication(String appId) {
		Application application  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications/" + appId;
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			application = ModelConverter.xmlApplicationToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return application;
	}

	@Override
	public Collection getDeployments(String appId) {
		Collection collection  = null;
		Boolean exception = false;
		String targetUrl = url + "/application/" + appId + "/deployments";
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			collection = ModelConverter.xmlCollectionToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return collection;
	}

	@Override
	public Deployment getDeployment(String appId, String deploymentId) {
		Deployment deployment  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications/" + appId + "/deployments/" + deploymentId;
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			deployment = ModelConverter.xmlDeploymentToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return deployment;
	}

	@Override
	public Agreement getAgreement(String appId, String deploymentId) {
		Agreement agreement  = null;
		Boolean exception = false;
		String targetUrl = url + "/applications/" + appId + "/deployments/" + deploymentId + "/aggreement";
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.getMethod(targetUrl, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			agreement = ModelConverter.xmlAgreementToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return agreement;
	}

	@Override
	public Deployment createDeployment(String appId, String ovf) {
		Deployment deployment  = null;
		Boolean exception = false;
		String targetUrl = url + "/application/" + appId + "/deployments";
		logger.info("URL build: " + targetUrl);
		
		try {
			String response = Client.postMethod(targetUrl, ovf, Dictionary.CONTENT_TYPE_XML, Dictionary.CONTENT_TYPE_XML, exception);
			logger.info("PAYLOAD: " + response);
			deployment = ModelConverter.xmlDeploymentToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse XML response from AppMan: " + targetUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return deployment;
	}
	
}
