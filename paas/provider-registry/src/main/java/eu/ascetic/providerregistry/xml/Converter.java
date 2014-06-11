package eu.ascetic.providerregistry.xml;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import eu.ascetic.providerregistry.model.Provider;

/**
 * Converts objects from and to XML representation
 * @author David Garcia Perez - Atos
 */
public class Converter {
	private static Logger logger = Logger.getLogger(Converter.class);

	/**
	 * Converts and XML string into an Provider object representation if possible
	 * @param xml to be parsed
	 * @return the Provider object or null if any error occurs...
	 */
	public static Provider getProviderObject(String xml) {	
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Provider provider = (Provider) jaxbUnmarshaller.unmarshal(new StringReader(xml));
			return provider;
		} catch(JAXBException exception) {
			logger.info("Error parsing XML of provider: " + exception.getMessage());
			return null;
		}
	}
	
	/**
	 * Returns an XML representation of an Provider object
	 * @param provider object to be converted to XML
	 * @return XML string in case any exception in the conversion, it will return null
	 */
	public static String getProviderXML(Provider provider) {		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(provider, out);
			String output = out.toString();
			logger.debug("Converting provider object to XML: ");
			logger.debug(output);
			
			return output;
		} catch(JAXBException exception) {
			logger.info("Error converting Provider object to XML: " + exception.getMessage());
			return null;
		}
	}
}
