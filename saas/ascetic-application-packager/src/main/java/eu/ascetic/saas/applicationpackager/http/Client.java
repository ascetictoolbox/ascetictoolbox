package eu.ascetic.saas.applicationpackager.http;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import eu.ascetic.saas.applicationpackager.Dictionary;
import eu.ascetic.saas.applicationpackager.ide.wizards.progressDialogs.AppManagerCallProgressBarDialog;



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
 * @author David Garcia Perez, David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net, david.rojoa@atos.net 
 * 
 * Basic REST methods implemented using the HTTPClient Apache Libraries
 *
 */

public class Client {
	private static Logger logger = Logger.getLogger(Client.class);
	/** The http client. */
	protected static HttpClient httpClient;
	
	/**
	 * Gets the http client.
	 *
	 * @return the http client
	 */
	private static HttpClient getHttpClient() {
		if(httpClient == null) {
			httpClient = new HttpClient();
		}		
		return httpClient;
	}
	
	/**
	 * Implements an HTTP GET Method
	 *
	 * @param url the url
	 * @param exeception if something goes wrong in the connection, the object is set to null
	 * @return the method
	 */
	public static String getMethod(String url, String accept, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();
				
//		logger.debug("Connecting to: " + url);
		// Create a method instance.
		GetMethod method = new GetMethod(url);
		setHeaders(method, accept);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) { //TODO test for this case... 
				logger.warn("Execution of GET method to: " + url + " failed: " + method.getStatusLine());
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
	private static void setHeaders(HttpMethod method, String accept) {
		method.addRequestHeader("User-Agent", Dictionary.USER_AGENT);
		method.addRequestHeader("Accept", accept);	
	}
	
	/**
	 * POST method
	 * @param url to which to perform a REST POST request
	 * @param payload payload of the request
	 * @param accept type of the response the method accepts
	 * @param contentType type of the response the method gets back
	 * @param dialog 
	 * @param exeception if something goes wrong in the connection, the object is set to null
	 * @return the response payload
	 */
	public static String postMethod(String url, String payload, String accept, String contentType, Boolean exception, AppManagerCallProgressBarDialog dialog) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.info("Connecting to: " + url);
		System.out.println("Connecting to: " + url);
		if (dialog != null){
			dialog.addLogMessage("Connecting to: " + url);
			dialog.updateProgressBar(5);
		}
		// Create a method instance.
		PostMethod method = new PostMethod(url);
		setHeaders(method, accept);
		method.addRequestHeader("Content-Type", contentType);
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// We set the payload
			StringRequestEntity payloadEntity = new StringRequestEntity(payload,  Dictionary.CONTENT_TYPE_JSON, "UTF-8");
			method.setRequestEntity(payloadEntity);
			
			// Execute the method.
			int statusCode = client.executeMethod(method);
			logger.info("Status Code: " + statusCode );
			System.out.println("Status Code: " + statusCode );
			if (dialog != null){
				dialog.addLogMessage("Status Code: " + statusCode);
			}

			if (statusCode >= 200 && statusCode > 300) { //TODO test for this case... 
				logger.info("Execution of POST method to: " + url + " failed: " + method.getStatusLine());
				System.out.println("Execution of POST method to: " + url + " failed: " + method.getStatusLine());
				if (dialog != null){
					dialog.addLogMessage("Execution of POST method to: " + url + " failed: " + method.getStatusLine());
				}
			} else {
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				response = new String(responseBody);
			}	

		} catch(HttpException e) {
			logger.info("Fatal protocol violation: " + e.getMessage());
			System.out.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			if (dialog != null){
				dialog.addLogMessage("Fatal protocol violation: " + e.getMessage());
			}
			exception = true;
		} catch(IOException e) {
			logger.info("Fatal transport error: " + e.getMessage());
			System.out.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
			if (dialog != null){
				dialog.addLogMessage("Fatal transport error: " + e.getMessage());
			}
			exception = true;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return response;
	}
	
	
	public static String postMethod(String url, String payload, String accept, String contentType, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.info("Connecting to: " + url);
		System.out.println("Connecting to: " + url);
		// Create a method instance.
		PostMethod method = new PostMethod(url);
		setHeaders(method, accept);
		method.addRequestHeader("Content-Type", contentType);
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		String response = "";

		try {
			// We set the payload
			StringRequestEntity payloadEntity = new StringRequestEntity(payload,  Dictionary.CONTENT_TYPE_JSON, "UTF-8");
			method.setRequestEntity(payloadEntity);
			
			// Execute the method.
			int statusCode = client.executeMethod(method);
			logger.info("Status Code: " + statusCode );
			System.out.println("Status Code: " + statusCode );

			if (statusCode >= 200 && statusCode > 300) { //TODO test for this case... 
				logger.info("Execution of POST method to: " + url + " failed: " + method.getStatusLine());
				System.out.println("Execution of POST method to: " + url + " failed: " + method.getStatusLine());
			} else {
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				response = new String(responseBody);
			}	

		} catch(HttpException e) {
			logger.info("Fatal protocol violation: " + e.getMessage());
			System.out.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
			exception = true;
		} catch(IOException e) {
			logger.info("Fatal transport error: " + e.getMessage());
			System.out.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();

			exception = true;
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return response;
	}
	
	/**
	 * PUT REST Method
	 * @param url to do the PUT method
	 * @param payload payload to change the properties of the entity
	 * @param accept type of respose expected
	 * @param contentType type of message sent
	 * @param exeception if something goes wrong in the connection, the object is set to null
	 * @return the updated entity
	 */
	public static String putMethod(String url, String payload, String accept, String contentType, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.debug("Connecting to: " + url);
		// Create a method instance.
		PutMethod method = new PutMethod(url);
		setHeaders(method, accept);
		method.addRequestHeader("Content-Type", contentType);
		
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
				logger.warn("Execution of PUT method to: " + url + " failed: " + method.getStatusLine());
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
	 * DELETE REST Method
	 * @param url to do the PUT method
	 * @param payload payload to change the properties of the entity
	 * @param accept type of respose expected
	 * @param exeception if something goes wrong in the connection, the object is set to null
	 * @return the updated entity
	 */
	public static void deleteMethod(String url, String accept, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.debug("Connecting to: " + url);
		// Create a method instance.
		DeleteMethod method = new DeleteMethod(url);
		setHeaders(method, accept);
		
		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_NO_CONTENT) { //TODO test for this case... 
				logger.warn("Execution of DELETE method to: " + url + " failed: " + method.getStatusLine());
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

	}
}
