package eu.ascetic.paas.applicationmanager.http;

import java.io.IOException;

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

import eu.ascetic.paas.applicationmanager.Dictionary;

/**
 * Basic REST methods implemented using the HTTPClient Apache Libraries
 * @author David Garcia Perez - Atos
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
				
		logger.debug("Connecting to: " + url);
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
	 * @param exeception if something goes wrong in the connection, the object is set to null
	 * @return the response payload
	 */
	public static String postMethod(String url, String payload, String accept, String contentType, Boolean exception) {
		// Create an instance of HttpClient.
		HttpClient client = getHttpClient();

		logger.debug("Connecting to: " + url);
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
}
