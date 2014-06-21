package eu.ascetic.paas.applicationmanager.model.converter;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Collection;

/**
 * Converts XML representations and viceversa
 * @author David Garcia Perez - Atos
 */
public class ModelConverter {
	private static Logger logger = Logger.getLogger(ModelConverter.class);

	/**
	 * Converts a Collection object to its String XML representation
	 * @param collection object to be converted
	 * @return XML representation
	 */
	public static String objectCollectionToXML(Collection collection) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(collection, out);
			String output = out.toString();
			logger.debug("Converting Collection object to XML: ");
			logger.debug(output);
			
			return output;
		} catch(Exception exception) {
			logger.info("Error converting Collection object to XML: " + exception.getMessage());
			return null;
		}
	}
	
	/**
	 * Converts an XML to a Collection object
	 * @param xml Representation of an Collection of Applications
	 * @return the Collection object or null if the xml is mal-formatted
	 */
	public static Collection xmlCollectionToObject(String xml) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
			return collection;
		} catch(JAXBException exception) {
			logger.info("Error parsing XML of Collection: " + exception.getMessage());
			return null;
		}
	}
	
	/**
	 * Converts a Application object to its String XML representation
	 * @param application object to be converted
	 * @return XML representation
	 */
	public static String objectApplicationToXML(Application application) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(application, out);
			String output = out.toString();
			logger.debug("Converting Application object to XML: ");
			logger.debug(output);
			
			return output;
		} catch(Exception exception) {
			logger.info("Error converting Application object to XML: " + exception.getMessage());
			return null;
		}
	}
	
	/**
	 * Converts an Application XML to object representation
	 * @param xml to be converted
	 * @return an application object or null in case the XML is mal-formatted
	 */
	public static Application xmlApplicationToObject(String xml) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Application.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Application application = (Application) jaxbUnmarshaller.unmarshal(new StringReader(xml));
			return application;
		} catch(JAXBException exception) {
			logger.info("Error parsing XML of Application: " + exception.getMessage());
			return null;
		}
	}
}
