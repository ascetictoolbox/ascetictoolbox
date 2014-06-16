package eu.ascetic.providerregistry.xml;

import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_XML;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import eu.ascetic.providerregistry.model.Collection;
import eu.ascetic.providerregistry.model.Items;
import eu.ascetic.providerregistry.model.Link;
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
	
	/**
	 * Returns an XML representing a Collection of Providers
	 * @param providers list of providers to build the XML file
	 * @return the XML string
	 */
	public static String getRootCollectionXML(List<Provider> providers) {
		Collection collection = new Collection();
		collection.setHref("/");
		
		Link link = new Link();
		link.setRel("self");
		link.setType(CONTENT_TYPE_XML);;
		link.setHref("/");
		collection.addLink(link);
		
		if(providers != null) {
			Items items = new Items();
			items.setOffset(0);
			items.setTotal(providers.size());
			
			Link linkParent = new Link();
			linkParent.setRel("parent");
			linkParent.setType(CONTENT_TYPE_XML);;
			linkParent.setHref("/");
			
			for(Provider provider : providers) {
				provider.setHref("/" + provider.getId());
				provider.addLink(linkParent);
				
				Link linkSelf = new Link();
				linkSelf.setRel("self");
				linkSelf.setType(CONTENT_TYPE_XML);
				linkSelf.setHref(provider.getHref());
				provider.addLink(linkSelf);
				
				items.addProvider(provider);
			}
			
			collection.setItems(items);
		}
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(collection, out);
			String output = out.toString();
			logger.debug("Converting collection of providers object to XML: ");
			logger.debug(output);
			
			return output;
		} catch(JAXBException exception) {
			logger.info("Error converting collection of providers object to XML: " + exception.getMessage());
			return null;
		}
	}
}
