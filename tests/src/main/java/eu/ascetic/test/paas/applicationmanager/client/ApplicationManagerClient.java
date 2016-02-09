package eu.ascetic.test.paas.applicationmanager.client;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import com.google.common.net.MediaType;

import eu.ascetic.paas.applicationmanager.model.Root;
import eu.ascetic.test.conf.Configuration;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 *  
 * REST Client to the Application Manager
 */
public class ApplicationManagerClient {
	private static Logger logger = Logger.getLogger(ApplicationManagerClient.class);

	/**
	 * @return returns the actual application manager version
	 */
	public static String getApplicationManagerVersion() {
		RestTemplate restTemplate = new RestTemplate();
		Root root = restTemplate.getForObject(Configuration.applicationManagerURL, Root.class);
		return root.getVersion();
	}
	
	/**
	 * Old Post using HttpClient, for uploading the ovf.
	 * @param url
	 * @param payload
	 * @return
	 */
	private String postMethod(String url, String payload) {
		HttpClient httpClient = new HttpClient();
		
		logger.info("Starting POST method to url: " + url);
		
		PostMethod post = new PostMethod(url);
		post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		
		String response = "";
		
		try {
			// We set the payload
			StringRequestEntity payloadEntity = new StringRequestEntity(payload, "application/xml", "UTF-8");
			post.setRequestEntity(payloadEntity);
			
			// Execute method
			int statusCode = httpClient.executeMethod(post);
			logger.info("POST method response: " + statusCode);
			
			if(statusCode >= 200 && statusCode > 300) {
				// Error in the response
				logger.warn("Post method error: " + url + " failed: " + post.getStatusLine());
			} else {
				// Read the response
				byte[] responseBody = post.getResponseBody();
				response = new String(responseBody);
			}
		} catch(HttpException e) {
			logger.warn("Fatal protocol violation: " + e.getStackTrace());		
		} catch(IOException e) {
			logger.warn("Fatal transportb error: " + e.getStackTrace());
		} finally {
			post.releaseConnection();
		}
		
		return response;
	}
}
