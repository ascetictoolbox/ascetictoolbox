package net.atos.ari.seip.CloudManager.REST.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class HTTPOperations {

	private static Client setClient() {
		ClientConfig config = new DefaultClientConfig();
		return Client.create(config);
	}
	
	
	public static String doGET(String url,String acceptContentType, String producesContentType, Class<String> classType){
		Client client = setClient();
		WebResource service = client.resource(url);

		ClientResponse response = service.accept(acceptContentType).type(producesContentType).get(
				ClientResponse.class);
		int status = response.getStatus();
		String textEntity = response.getEntity(classType);
		return textEntity;
	}
	
	public static String doDELETE(String url,String acceptContentType, String producesContentType, Class<String> classType){
		Client client = setClient();
		WebResource service = client.resource(url);

		ClientResponse response = service.accept(acceptContentType).type(producesContentType).delete(
				ClientResponse.class);
		int status = response.getStatus();
		String textEntity = response.getEntity(classType);
		return textEntity;
	}
	
	public static String doPOST(String url, String content,String acceptContentType, String producesContentType, Class<String> classType){
		Client client = setClient();
		WebResource service = client.resource(url);

		ClientResponse response = service.accept(acceptContentType).type(producesContentType).post(
				ClientResponse.class, content);
		int status = response.getStatus();
		String textEntity = response.getEntity(classType);
		return textEntity;
	}
}
