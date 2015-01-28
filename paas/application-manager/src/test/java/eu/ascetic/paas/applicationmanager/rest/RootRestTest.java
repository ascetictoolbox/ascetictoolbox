package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.model.Root;

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
 */

public class RootRestTest {

	@Test
	public void getRootTest() throws JAXBException {
		RootRest rootRest = new RootRest();
		
		Response response = rootRest.getRoot();
		String timestamp = "" + System.currentTimeMillis();
		timestamp = timestamp.substring(0, timestamp.length() - 4);
		
		assertEquals(200, response.getStatus());
		
		String providersXML = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Root.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Root root = (Root) jaxbUnmarshaller.unmarshal(new StringReader(providersXML));
		
		assertEquals("/", root.getHref());
		assertEquals("0.1-SNAPSHOT", root.getVersion());
		assertEquals(timestamp, root.getTimestamp().substring(0, root.getTimestamp().length() - 4));
		assertEquals(1, root.getLinks().size());
		assertEquals("applications", root.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, root.getLinks().get(0).getType());
		assertEquals("/applications", root.getLinks().get(0).getHref());
	}
}
