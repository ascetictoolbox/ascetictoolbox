package eu.ascetic.paas.applicationmanager.spreader.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.Test;

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
 * Test that verifies the correct work of the Class IaaSMessage
 *
 *
 */
public class IaaSMessageTest {
	private String json = "{\n" 
							+ "   \"name\" : \"name\",\n" 
							+ "   \"value\" : 0.1,\n"
							+ "   \"units\" : \"units\",\n"
							+ "   \"timestamp\" : 22\n"
						+ "}";

	@Test
	public void pojo() {
		IaaSMessage im = new IaaSMessage();
		im.setName("name");
		im.setTimestamp(22l);
		im.setUnits("units");
		im.setValue(0.1);
		
		assertEquals("name", im.getName());
		assertEquals("units", im.getUnits());
		assertEquals(22l, im.getTimestamp());
		assertEquals(0.1, im.getValue(), 0.00001);
	}
	
	@Test
	public void conversionToJSON() {
		IaaSMessage im = new IaaSMessage();
		im.setName("name");
		im.setTimestamp(22l);
		im.setUnits("units");
		im.setValue(0.1);
		
	    try {
	    	JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {IaaSMessage.class}, null);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			marshaller.marshal(im, out);
			String output = out.toString();
			
			assertEquals(json, output);
			
		} catch(Exception exception) {
			System.out.println(exception.toString());
			fail("Impossible to convert to JSON the IaaS Message object...");
		}
	}
	
	@Test
	public void conversionFromJsonToObject() {
	
		try {
	        // Create a JaxBContext
			JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {IaaSMessage.class}, null);
	        
	        // Create the Unmarshaller Object using the JaxB Context
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
	        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
	        
	        StreamSource jsonSource = new StreamSource(new StringReader(json));  
	        IaaSMessage im = unmarshaller.unmarshal(jsonSource, IaaSMessage.class).getValue();
			
			assertEquals("name", im.getName());
			assertEquals("units", im.getUnits());
			assertEquals(22l, im.getTimestamp());
			assertEquals(0.1, im.getValue(), 0.00001);
		} catch(Exception exception) {
			System.out.println(exception.toString());
			fail("Impossible to convert to object from the JSON IaaS Message...");
		} 
	}
}
