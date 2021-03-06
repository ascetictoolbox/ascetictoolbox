package eu.ascetic.paas.slam.pac.applicationmanager;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import eu.ascetic.paas.slam.pac.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Agreement;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Application;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Collection;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Deployment;
import eu.ascetic.paas.slam.pac.applicationmanager.model.EnergyMeasurement;
import eu.ascetic.paas.slam.pac.applicationmanager.model.PowerMeasurement;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Root;
import eu.ascetic.paas.slam.pac.applicationmanager.model.VM;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Converts XML representations and viceversa
 * 
 */

public class ModelConverter {
	private static Logger logger = Logger.getLogger(ModelConverter.class);

	/**
	 * Converts a Collection object to its String XML representation
	 * @param collection object to be converted
	 * @return XML representation
	 */
	public static String objectCollectionToXML(Collection collection) {	
		return toXML(Collection.class, collection);
	}
	
	/**
	 * Converts an XML to a Collection object
	 * @param xml Representation of an Collection of Applications
	 * @return the Collection object or null if the xml is mal-formatted
	 */
	public static Collection xmlCollectionToObject(String xml) {
		return toObject(Collection.class, xml);
	}
	
	/**
	 * Converts a Application object to its String XML representation
	 * @param application object to be converted
	 * @return XML representation
	 */
	public static String objectApplicationToXML(Application application) {
		return toXML(Application.class, application);
	}
	
	/**
	 * Converts an Application XML to object representation
	 * @param xml to be converted
	 * @return an application object or null in case the XML is mal-formatted
	 */
	public static Application xmlApplicationToObject(String xml) {
		return toObject(Application.class, xml);
	}
	
	/**
	 * Converts a Root object to its String XML representation
	 * @param root object to be converted 
	 * @return XML representation of the object
	 */
	public static String objectRootToXML(Root root) {
		return toXML(Root.class, root);
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
	
	private static <T> T toObject(Class<T> clazz, String xml) {
		try {
//			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			// Create a JaxBContext
			JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {clazz}, null);
				        
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
	
	/**
	 * Converts an ApplicationManagerMessage object to its JSON representation
	 * @param amMessage the object to be converted
	 * @return the JSON representation of the object
	 */
	public static String applicationManagerMessageToJSON(ApplicationManagerMessage amMessage) {
		return toJSON(ApplicationManagerMessage.class, amMessage);
	}
	
	/**
	 * Converts a json string into a ApplicationManagerMessage object
	 * @param json to be converted
	 * @return the ApplicaitonManagerMessage represented by that string
	 */
	public static ApplicationManagerMessage jsonToApplicationManagerMessage(String json) {
		return fromJSONToObject(ApplicationManagerMessage.class, json);
	}

	/**
	 * Converts an xml Deployment represetnation to object
	 * @param xml to be converted to object
	 * @return the object representation of the deployment, <code>null</code> otherwise
	 */
	public static Deployment xmlDeploymentToObject(String xml) {
		return toObject(Deployment.class, xml);
	}

	/**
	 * Converts an object Deployment to its XML representation
	 * @param deployment object to be converted to XML
	 * @return the XML representation of the Deployment object
	 */
	public static String objectDeploymentToXML(Deployment deployment) {
		return toXML(Deployment.class, deployment);
	}

	/**
	 * Covnerts an xml VM to its object represetnation
	 * @param xml to be converted to object
	 * @return the object resulting of converting the previously XML or <code>null</code> if the XMl is invalid.
	 */
	public static VM xmlVMToObject(String xml) {
		return toObject(VM.class, xml);
	}

	/**
	 * Converts an object VM to its XML representation
	 * @param vm object to be converted to XML
	 * @return the XML representation of the VM object
	 */
	public static String objectVMToXML(VM vm) {
		return toXML(VM.class, vm);
	}

	/**
	 * Converts an xml representation of an Agreement to object
	 * @param xml representation of the agreement
	 * @return object represenation of the agreement
	 */
	public static Agreement xmlAgreementToObject(String xml) {
		return toObject(Agreement.class, xml);
	}

	/**
	 * Converts an object Agreement to xml representation
	 * @param agreement object to be converted to xml
	 * @return string xml representation of the agreement object.
	 */
	public static String objectAgreementToXML(Agreement agreement) {
		return toXML(Agreement.class, agreement);
	}
	
	/**
	 * Converts a EnergyMeasurement object to its String XML representation
	 * @param energyMeasurement object to be converted
	 * @return XML representation
	 */
	public static String objectEnergyMeasurementToXML(EnergyMeasurement energyMeasurement) {	
		return toXML(EnergyMeasurement.class, energyMeasurement);
	}
	
	/**
	 * Converts an XML to a EnergyMeasurement object
	 * @param xml Representation of an Collection of Applications
	 * @return the EnergyMeasurement object or null if the xml is mal-formatted
	 */
	public static EnergyMeasurement xmlEnergyMeasurementToObject(String xml) {
		return toObject(EnergyMeasurement.class, xml);
	}
	
	/**
	 * Converts a PowerMeasurement object to its String XML representation
	 * @param powerMeasurement object to be converted
	 * @return XML representation
	 */
	public static String objectPowerMeasurementToXML(PowerMeasurement powerMeasurement) {	
		return toXML(PowerMeasurement.class, powerMeasurement);
	}
	
	/**
	 * Converts an XML to a EnergyMeasurement object
	 * @param xml Representation of an Collection of Applications
	 * @return the PowerMeasurement object or null if the xml is mal-formatted
	 */
	public static PowerMeasurement xmlPowerMeasurementToObject(String xml) {
		return toObject(PowerMeasurement.class, xml);
	}
}
