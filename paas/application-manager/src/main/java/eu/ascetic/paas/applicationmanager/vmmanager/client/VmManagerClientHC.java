package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVms;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.converter.ModelConverter;



/**
 * The Class VmManagerClientHC.
 */
public class VmManagerClientHC implements VmManagerClient {

	/** The logger. */
	private static Logger logger = Logger.getLogger(VmManagerClientHC.class);
	
	/** The url. */
	protected String url;
	
	/** The http client. */
	protected HttpClient httpClient;
	
	/**
	 * Instantiates a new vm manager client hc.
	 */
	public VmManagerClientHC() {
		this.url = Configuration.vmManagerServiceUrl;
	}
	
	/**
	 * Instantiates a new vm manager client hc.
	 *
	 * @param url the url
	 */
	public VmManagerClientHC(String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getURL()
	 */
	@Override
	public String getURL() {
		return url;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#setURL(java.lang.String)
	 */
	@Override
	public void setURL(String url) {
		this.url = url;
	}
	
	/**
	 * Gets the http client.
	 *
	 * @return the http client
	 */
	private HttpClient getHttpClient() {
		if(httpClient == null) {
			httpClient = new HttpClient();
		}		
		return httpClient;
	}
	
	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getListOfImagesUploaded()
	 */
	@Override
	public ListImagesUploaded getAllImages() {
		Boolean exception = false;
		String testbedsUrl = url + "/images";
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = getMethod(testbedsUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListImagesUploaded imagesUploaded = null;
		
		try {
			imagesUploaded = ModelConverter.jsonListImagesUploadedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of images: " + url + "/images" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return imagesUploaded;
	}
	
	/**
	 * Gets the method.
	 *
	 * @param url the url
	 * @param userId the user id
	 * @param groupId the group id
	 * @param exception the exception
	 * @return the method
	 */
	private String getMethod(String url, String userId, String groupId, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();
				
		logger.debug("Connecting to: " + url);
		// Create a method instance.
		GetMethod method = new GetMethod(url);
		setHeaders(method, groupId, userId);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) { //TODO test for this case... 
				logger.warn("Get host information of testbeds: " + url + " failed: " + method.getStatusLine());
			} else {
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				response = new String(responseBody);
			}	

		} catch(HttpException e) {
			logger.warn("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} catch(IOException e) {
			logger.warn("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		
		return response;
	}
	
	/**
	 * Sets the headers.
	 *
	 * @param method the method
	 * @param groupId the group id
	 * @param userId the user id
	 */
	private void setHeaders(HttpMethod method, String groupId, String userId) {
		method.addRequestHeader("User-Agent", Dictionary.USER_AGENT);
		method.addRequestHeader("Accept", Dictionary.ACCEPT);	
	}
	
	private String postMethod(String url, String payload, String zabbixUserId, String zabbixGroupId, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.debug("Connecting to: " + url);
		// Create a method instance.
		PostMethod method = new PostMethod(url);
		setHeaders(method, zabbixGroupId, zabbixUserId);
		//method.addRequestHeader("Content-Type", SchedulerDictionary.CONTENT_TYPE_ECO2CLOUDS_XML);
		
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// We set the payload
			StringRequestEntity payloadEntity = new StringRequestEntity(payload,  Dictionary.CONTENT_TYPE_JSON, "UTF-8");
			method.setRequestEntity(payloadEntity);
			
			// Execute the method.
			int statusCode = client.executeMethod(method);
			logger.debug("Status Code: " + statusCode );

			if (statusCode >= 200 && statusCode > 300) { //TODO test for this case... 
				logger.warn("Get host information of testbeds: " + url + " failed: " + method.getStatusLine());
			} else {
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				response = new String(responseBody);
			}	

		} catch(HttpException e) {
			logger.warn("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} catch(IOException e) {
			logger.warn("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return response;
	}
	
	private String putMethod(String url, String payload, String zabbixUserId, String zabbixGroupId, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.debug("Connecting to: " + url);
		// Create a method instance.
		PutMethod method = new PutMethod(url);
		setHeaders(method, zabbixGroupId, zabbixUserId);
		
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// We set the payload
			StringRequestEntity payloadEntity = new StringRequestEntity(payload,  Dictionary.CONTENT_TYPE_JSON, "UTF-8");
			method.setRequestEntity(payloadEntity);
			
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) { //TODO test for this case... 
				logger.warn("Get host information of testbeds: " + url + " failed: " + method.getStatusLine());
			} else {
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				response = new String(responseBody);
			}	

		} catch(HttpException e) {
			logger.warn("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} catch(IOException e) {
			logger.warn("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return response;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getImage(java.lang.String)
	 */
	@Override
	public ImageUploaded getImage(String id) {
		Boolean exception = false;
		
		String imageUrl = url + "/images/" + id;
		String response = getMethod(imageUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		ImageUploaded imageUploaded = null;
	
		
		try {
			imageUploaded = ModelConverter.jsonImageUploadedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned imageUploaded: " + imageUrl + " Exception: " + e.getMessage());
			exception = true;
		}
			
		if(exception) return null;
		
		return imageUploaded;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getAllVMs()
	 */
	@Override
	public ListVmsDeployed getAllVMs() {
		Boolean exception = false;
		String testbedsUrl = url + "/vms";
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = getMethod(testbedsUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListVmsDeployed vms = null;
		
		try {
			vms = ModelConverter.jsonListVmsDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of vms deployed: " + testbedsUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return vms;
	}

	@Override
	public VmDeployed getVM(String id) {
		Boolean exception = false;
		
		String imageUrl = url + "/vms/" + id;
		String response = getMethod(imageUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		VmDeployed vm = null;
	
		
		try {
			vm = ModelConverter.jsonVmDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned vm deployed: " + imageUrl + " Exception: " + e.getMessage());
			exception = true;
		}
			
		if(exception) return null;
		
		return vm;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getVmsOfApp(java.lang.String)
	 */
	@Override
	public ListVmsDeployed getVmsOfApp(String appId) {
		Boolean exception = false;
		String testbedsUrl = url + "/vmsapp" + appId;
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = getMethod(testbedsUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		ListVmsDeployed vmsOfApp = null;
		
		try {
			vmsOfApp = ModelConverter.jsonListVmsDeployedToObject(response);
		} catch(Exception e) {
			logger.warn("Error trying to parse returned list of vms deployed: " + testbedsUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return vmsOfApp;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#getLogs()
	 */
	@Override
	public String getLogs() {
		Boolean exception = false;
		String testbedsUrl = url + "/logs";
		
		logger.debug("CONNECTING TO: " + url);
		
		String response = getMethod(testbedsUrl, null, null, exception);
		logger.debug("PAYLOAD: " + response);
		
		String log = null;
		
		try {
			log = response;
		} catch(Exception e) {
			logger.warn("Error trying to return logs: " + testbedsUrl + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return log;
	}
	


	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#deployVMs(java.util.List)
	 */
	@Override
	public List<String> deployVMs(List<Vm> vms) {
		List<String> listIDs = null;
		Boolean exception = false;
		String experimentUrl = url + "/vms/";
		logger.debug("URL build: " + experimentUrl);
		
		try {
			ListVms listVms = new ListVms(vms);
			String payload = ModelConverter.objectListVmsToJSON(listVms);
			
			String response = postMethod(experimentUrl, payload, /*user*/null, /*groupId*/null, exception);
			logger.debug("PAYLOAD: " + response);
			
			try {
				listIDs = ModelConverter.jsonListStringToObject(response);
			} catch(Exception e) {
				logger.warn("Error trying incoming list of new IDs to object. Exception: " + e.getMessage());
				exception = true;
			}
		} catch(Exception e) {
			logger.warn("Error trying to parse list of VMs: " + url + "/vms" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		return listIDs;
	}

	/* (non-Javadoc)
	 * @see eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClient#uploadImage(eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload)
	 */
	@Override
	public String uploadImage(ImageToUpload imageInfo) {
		String newImageID = null;
		Boolean exception = false;
		String experimentUrl = url + "/images/";
		logger.debug("URL build: " + experimentUrl);
		
		try {
			String payload = ModelConverter.objectImageToUploadToJSON(imageInfo);
			
			String response = postMethod(experimentUrl, payload, /*user*/null, /*groupId*/null, exception);
			logger.debug("PAYLOAD: " + response);
			
			try {
				newImageID = ModelConverter.jsonStringIdToObject(response);
			} catch(Exception e) {
				logger.warn("Error trying incoming list of new IDs to object. Exception: " + e.getMessage());
				exception = true;
			}
		} catch(Exception e) {
			logger.warn("Error trying to parse new image uploaded ID: " + url + "/images" + " Exception: " + e.getMessage());
			exception = true;
		}
		
		if(exception) return null;
		
		return newImageID;
	}

	@Override
	public void deleteVmsOfApp(String appId) {
		// TODO Auto-generated method stub
		
	}
}
