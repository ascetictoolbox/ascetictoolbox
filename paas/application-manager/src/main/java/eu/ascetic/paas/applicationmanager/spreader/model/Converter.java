package eu.ascetic.paas.applicationmanager.spreader.model;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

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
 * Converts from and to JSON the monitoring messages from the IaaS layer
 *
 *
 */
public class Converter {
	private static Logger logger = Logger.getLogger(Converter.class);
	
	/**
	 * It converts a json string to an IaaSMessage
	 * @param json the json string to be converted to object
	 * @return the converted object, null if there was any error
	 */
	public static IaaSMessage iaasMessageFromJSONToObject(String json) {
		try {
	        // Create a JaxBContext
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {IaaSMessage.class}, null);
	        
	        // Create the Unmarshaller Object using the JaxB Context
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
	        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
	        
	        StreamSource jsonSource = new StreamSource(new StringReader(json));  
	        IaaSMessage im = unmarshaller.unmarshal(jsonSource, IaaSMessage.class).getValue();
			
			return im;
		} catch(Exception exception) {
			logger.error(exception.getMessage());
			return null;
		} 
	}
	
	/**
	 * Converts an IaaSMessage object to JSON
	 * @param im
	 * @return
	 */
	public static String iaasMessageToJSON(IaaSMessage im) {
	    try {
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {IaaSMessage.class}, null);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(im, out);
			String output = out.toString();
			logger.debug("Converting object to JSON: ");
			logger.debug(output);
			
			return output;
		} catch(Exception exception) {
			logger.info("Error converting IaaSMessage object to JSON: " + exception.getMessage());
			return null;
		}      
	}
}
