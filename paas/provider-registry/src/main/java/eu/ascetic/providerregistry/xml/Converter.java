package eu.ascetic.providerregistry.xml;

import static eu.ascetic.providerregistry.Dictionary.CONTENT_TYPE_XML;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import eu.ascetic.providerregistry.model.Collection;
import eu.ascetic.providerregistry.model.Items;
import eu.ascetic.providerregistry.model.Link;
import eu.ascetic.providerregistry.model.Provider;

/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * @email david.garciaperez@atos.net 
 * 
 * Converts objects from and to XML representation
 */
public class Converter {
	private static Logger logger = Logger.getLogger(Converter.class);

	/**
	 * Converts and XML string into an Provider object representation if possible
	 * @param xml to be parsed
	 * @return the Provider object or null if any error occurs...
	 */
	public static Provider getProviderObject(String xml) {	
		
		return toObject(Provider.class, xml);
	}
	
	/**
	 * Returns an XML representation of an Provider object
	 * @param provider object to be converted to XML
	 * @return XML string in case any exception in the conversion, it will return null
	 */
	public static String getProviderXML(Provider provider) {	
		
		return toXML(Provider.class, provider);
	}
	
	public static String getRootCollectionJSON(List<Provider> providers) {
		Collection collection = prepareCollection(providers);
		
		return toJSON(Collection.class, collection);
	}
	
	private static Collection prepareCollection(List<Provider> providers) {
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
		
		return collection;
	}
	
	/**
	 * Returns an XML representing a Collection of Providers
	 * @param providers list of providers to build the XML file
	 * @return the XML string
	 */
	public static String getRootCollectionXML(List<Provider> providers) {
		Collection collection = prepareCollection(providers);
		
		return toXML(Collection.class, collection);
	}
	
	private static <T> String toXML(Class<T> clazz, T t) {
	    try {
	        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
	        Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(t, out);
			String output = out.toString();
			logger.debug("Converting object to XML: ");
			logger.debug(output);
			
			return output;
		} catch(Exception exception) {
			logger.info("Error converting object to XML: " + exception.getMessage());
			return null;
		}      
	}
	
	private static <T> String toJSON(Class<T> clazz, T t) {
	    try {
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {clazz}, null);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(t, out);
			String output = out.toString();
			logger.debug("Converting object to JSON: ");
			logger.debug(output);
			
			return output;
		} catch(Exception exception) {
			logger.info("Error converting object to XML: " + exception.getMessage());
			return null;
		}      
	}
	
	private static <T> T toObject(Class<T> clazz, String xml) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Object obj = jaxbUnmarshaller.unmarshal(new StringReader(xml));
			
			return clazz.cast(obj);
		} catch(Exception exception) {
			logger.info("Error parsing XML: " + exception.getMessage());
			return null;
		}    
	}
	
	private static <T> T fromJSONToObject(Class<T> clazz, String json) {
		try {
	        // Create a JaxBContext
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {clazz}, null);
	        
	        // Create the Unmarshaller Object using the JaxB Context
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
	        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
	        
	        StreamSource jsonSource = new StreamSource(new StringReader(json));  
	        T object = unmarshaller.unmarshal(jsonSource, clazz).getValue();
			
			return object;
		} catch(Exception exception) {
			logger.info("Error parsing XML: " + exception.getMessage());
			return null;
		}    
	}

	public static String getProviderJSON(Provider provider) {
		return toJSON(Provider.class, provider);
	}
	
	public static Provider getProviderFromJSON(String json) {
		return fromJSONToObject(Provider.class, json);
	}
}
