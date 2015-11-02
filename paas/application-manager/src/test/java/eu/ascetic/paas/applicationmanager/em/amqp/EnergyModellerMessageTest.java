package eu.ascetic.paas.applicationmanager.em.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.Test;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Unit test for the class EnergyModellerMessageTest
 */

public class EnergyModellerMessageTest {

	@Test
	public void pojoTest() {
		EnergyModellerMessage message = new EnergyModellerMessage();
		message.setApplicationid("0");
		message.setDeploymentid("1");
		message.setEventid("2");
		message.setGenerattiontimestamp("3");
		message.setReferredtimestamp("4");
		message.setUnit("5");
		message.setValue("6");
		
		assertEquals("0", message.getApplicationid());
		assertEquals("1", message.getDeploymentid());
		assertEquals("2", message.getEventid());
		assertEquals("3", message.getGenerattiontimestamp());
		assertEquals("4", message.getReferredtimestamp());
		assertEquals("5", message.getUnit());
		assertEquals("6", message.getValue());
	}
	
	@Test
	public void testJAXBJSONParsing() throws JAXBException {
		String message = "{" + 
							"\"provider\":null," + 
							"\"applicationid\":\"davidgpTestApp\"," + 
							"\"eventid\":\"loquesea\"," + 
							"\"deploymentid\":\"569\"," + 
							"\"vms\":[" +
								"\"1899\", " +
								"\"333\"" +
							"]," + 
							"\"unit\":\"SEC\"," + 
							"\"generattiontimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
							"\"referredtimestamp\":\"30 Sep 2015 16:29:35 GMT\"," + 
							"\"value\":0.0" +
						"}";
		
		 // Create a JaxBContext
		JAXBContext jc = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {EnergyModellerMessage.class}, null);
        
		// Create the Unmarshaller Object using the JaxB Context
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
        
        StreamSource jsonSource = new StreamSource(new StringReader(message));  
        EnergyModellerMessage emMessage = unmarshaller.unmarshal(jsonSource, EnergyModellerMessage.class).getValue();
        
		assertEquals("davidgpTestApp", emMessage.getApplicationid());
		assertEquals("569", emMessage.getDeploymentid());
		assertEquals("loquesea", emMessage.getEventid());
		assertEquals("30 Sep 2015 16:29:35 GMT", emMessage.getGenerattiontimestamp());
		assertEquals("30 Sep 2015 16:29:35 GMT", emMessage.getReferredtimestamp());
		assertEquals("SEC", emMessage.getUnit());
		assertEquals("0.0", emMessage.getValue());
		assertEquals(2, emMessage.getVms().size());
		assertEquals("1899", emMessage.getVms().get(0));
		assertEquals("333", emMessage.getVms().get(1));
	}
	
	@Test
	public void equalsTest() {
		List<String> vms1 = new ArrayList<String>();
		vms1.add("a");
		
		List<String> vms2 = new ArrayList<String>();
		vms2.add("a");
		
		EnergyModellerMessage message1 = new EnergyModellerMessage();
		message1.setApplicationid("0");
		message1.setDeploymentid("1");
		message1.setEventid("2");
		message1.setGenerattiontimestamp("3");
		message1.setReferredtimestamp("4");
		message1.setUnit("5");
		message1.setValue("6");
		message1.setProvider("7");
		message1.setVms(vms1);
		
		assertFalse(message1.equals(null));
		assertFalse(message1.equals(new Object()));
		
		EnergyModellerMessage message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertTrue(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("1");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("2");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("3");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("4");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("5");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("6");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("7");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("8");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
		
		vms2.add("b");
		message2 = new EnergyModellerMessage();
		message2.setApplicationid("0");
		message2.setDeploymentid("1");
		message2.setEventid("2");
		message2.setGenerattiontimestamp("3");
		message2.setReferredtimestamp("4");
		message2.setUnit("5");
		message2.setValue("6");
		message2.setProvider("7");
		message2.setVms(vms2);
		
		assertFalse(message1.equals(message2));
	}
}
