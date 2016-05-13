package eu.ascetic.saas.experimentmanager.wslayer;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.ascetic.saas.experimentmanager.wslayer.exception.IncorrectRessourceFormatException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.ResponseParsingException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSBaseException;
import eu.ascetic.saas.experimentmanager.wslayer.exception.WSException;

public class WSBasic {
	
	private static InputStream getFromUrl(String url,String accept,String post) throws WSException{
		Logger.getLogger("WSBasic").info("Querying " + url + " ... ");
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		ClientResponse response;
		if(post==null){
			response = webResource.accept(accept)
	                   .get(ClientResponse.class);
		}
		else{
			response = webResource.accept(accept).header("Content-Type", "text/plain")
                   .post(ClientResponse.class,post);
		}
		
		
		if (response.getStatus() != 200) {
			   throw new WSException(response.getStatus(),"Failed : HTTP error code for url "+url+" : "
				+ response.getStatus());
			}
		
		return response.getEntity(InputStream.class);
	}
	
	private static List<String> getList(String url, Handler handler, String query, String post) throws WSException, ResponseParsingException{
		InputStream is = getFromUrl(url, handler.getAccepted(),post);
		return handler.getList(is, query);
	}
	
	private static String getSingle(String url, Handler handler, String query, String post) throws WSException, ResponseParsingException{
		InputStream is = getFromUrl(url, handler.getAccepted(),post);
		return handler.getSingle(is, query);
	}

	private static Handler getHandler(RESSOURCEFORMAT format) throws IncorrectRessourceFormatException {
		switch (format){
		case JSON : return new JSONHandler();
		case XML : return new XMLHandler();
		default: throw new IncorrectRessourceFormatException("Ressource format is incorrect or empty");
		}
	}
	
	public static List<String> getList(String url, RESSOURCEFORMAT format, String query, String post) throws WSBaseException{
		return getList(url,getHandler(format),query,post);
	}
	
	public static List<String> getList(String url, RESSOURCEFORMAT format, String query) throws WSBaseException{
		return getList(url,getHandler(format),query,null);
	}
	
	public static String getSingle(String url, RESSOURCEFORMAT format, String query, String post) throws WSException, ResponseParsingException, IncorrectRessourceFormatException{
		return getSingle(url,getHandler(format),query,post);
	}
	
	public static String getSingle(String url, RESSOURCEFORMAT format, String query) throws WSBaseException{
		return getSingle(url,getHandler(format),query,null);
	}
	
}
