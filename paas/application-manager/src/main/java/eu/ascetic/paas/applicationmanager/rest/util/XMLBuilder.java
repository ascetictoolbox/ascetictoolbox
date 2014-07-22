package eu.ascetic.paas.applicationmanager.rest.util;

import javax.ws.rs.core.MediaType;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Link;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

/**
 * This class prepares objects that are going to be converted to XML to be sent from the REST interface
 * @author David Garcia Perez - Atos
 *
 */
public class XMLBuilder {

	/**
	 * Adds the necessary fields to an Application object to be able to send it as a reply by XML
	 * @param application object that needs the extra fiels
	 * @return the updated object
	 */
	public static Application addApplicationXMLInfo(Application application) {
		application.setHref("/applications/" + application.getId());
		
		Link linkParent = new Link();
		linkParent.setHref("/applications");
		linkParent.setRel("parent");
		linkParent.setType(MediaType.APPLICATION_XML);
		application.addLink(linkParent);
		
		Link linkSelf = new Link();
		linkSelf.setHref(application.getHref());
		linkSelf.setRel("self");
		linkSelf.setType(MediaType.APPLICATION_XML);
		application.addLink(linkSelf);
		
		return application;
	}
	
	public static String getXML(Application application) {
		application = addApplicationXMLInfo(application);
		return ModelConverter.objectApplicationToXML(application);
	}
}
