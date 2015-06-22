package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.dao.AgreementDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;

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
 * ASCETiC Application Manager REST API to perform actions over an agreements
 *
 */
public class AgreementRestTest extends AbstractTest {

	@Test
	public void getAgreementsTest() throws JAXBException {
		Agreement agreement1 = new Agreement();
		agreement1.setAccepted(false);
		agreement1.setId(1);
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id1");
		agreement1.setSlaAgreement("sssas1");
		agreement1.setSlaAgreementId("sla-agreement-id1");
		
		Agreement agreement2 = new Agreement();
		agreement2.setAccepted(true);
		agreement2.setId(2);
		agreement2.setPrice("222");
		agreement2.setProviderId("provider-id");
		agreement2.setSlaAgreement("sssas");
		agreement2.setSlaAgreementId("sla-agreement-id");
		
		Deployment deployment = new Deployment();
		deployment.addAgreement(agreement1);
		deployment.addAgreement(agreement2);
		
		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(223)).thenReturn(deployment);
		
		AgreementRest agreementRest = new AgreementRest();
		agreementRest.deploymentDAO = deploymentDAO;
		
		Response response = agreementRest.getAgreements("app-id", "223");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Collection.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Collection collection = (Collection) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getHref());
		assertEquals(0, collection.getItems().getOffset());
		assertEquals(2, collection.getItems().getTotal());
		// Links
		assertEquals(2, collection.getLinks().size());
		assertEquals("/applications/app-id/deployments/223", collection.getLinks().get(0).getHref());
		assertEquals("parent", collection.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements", collection.getLinks().get(1).getHref());
		assertEquals("self", collection.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, collection.getLinks().get(1).getType());
		// # Agreements
		assertEquals(2, collection.getItems().getAgreements().size());
		// Agreement #1
		Agreement agreement = collection.getItems().getAgreements().get(0);
		assertEquals("/applications/app-id/deployments/223/agreements/1", agreement.getHref());
		assertEquals("111", agreement.getPrice());
		assertEquals(1, agreement.getId());
		assertEquals("provider-id1", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id1", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/1", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
		// Agreement #2
		agreement = collection.getItems().getAgreements().get(1);
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals(2, agreement.getId());
		assertEquals("provider-id", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
	}
	
	@Test
	public void getAgreementTest() throws JAXBException {
		Agreement agreement = new Agreement();
		agreement.setAccepted(true);
		agreement.setId(2);
		agreement.setPrice("222");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sssas");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		AgreementDAO agreementDAO = mock(AgreementDAO.class);
		when(agreementDAO.getById(2)).thenReturn(agreement);
		
		AgreementRest agreementRest = new AgreementRest();
		agreementRest.agreementDAO = agreementDAO;
		
		Response response = agreementRest.getAgreement("app-id", "223", "2");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Agreement.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		agreement = (Agreement) jaxbUnmarshaller.unmarshal(new StringReader(xml));
		
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getHref());
		assertEquals("222", agreement.getPrice());
		assertEquals(2, agreement.getId());
		assertEquals("provider-id", agreement.getProviderId());
		//assertEquals("sssas", agreement.getSlaAgreement());  // This it is not converted to XML... 
		assertEquals("sla-agreement-id", agreement.getSlaAgreementId());
		assertEquals(2, agreement.getLinks().size());
		assertEquals("/applications/app-id/deployments/223/agreements", agreement.getLinks().get(0).getHref());
		assertEquals("parent", agreement.getLinks().get(0).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(0).getType());
		assertEquals("/applications/app-id/deployments/223/agreements/2", agreement.getLinks().get(1).getHref());
		assertEquals("self", agreement.getLinks().get(1).getRel());
		assertEquals(MediaType.APPLICATION_XML, agreement.getLinks().get(1).getType());
	}
	
	@Test
	public void getSlaAgreementTest() {
		Agreement agreement = new Agreement();
		agreement.setAccepted(true);
		agreement.setId(2);
		agreement.setPrice("222");
		agreement.setProviderId("provider-id");
		agreement.setSlaAgreement("sssas");
		agreement.setSlaAgreementId("sla-agreement-id");
		
		AgreementDAO agreementDAO = mock(AgreementDAO.class);
		when(agreementDAO.getById(2)).thenReturn(agreement);
		
		AgreementRest agreementRest = new AgreementRest();
		agreementRest.agreementDAO = agreementDAO;
		
		Response response = agreementRest.getSlaAgreement(null, null, "2");
		
		assertEquals(200, response.getStatus());
		
		String xml = (String) response.getEntity();
		assertEquals("sssas", xml);
	}
}
