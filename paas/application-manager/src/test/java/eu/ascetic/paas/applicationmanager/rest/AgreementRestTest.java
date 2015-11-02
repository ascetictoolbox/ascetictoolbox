package eu.ascetic.paas.applicationmanager.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slasoi.gslam.syntaxconverter.SLASOIRenderer;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.amqp.AbstractTest;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.AgreementDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.event.DeploymentEvent;
import eu.ascetic.paas.applicationmanager.event.deployment.DeploymentEventService;
import eu.ascetic.paas.applicationmanager.model.Agreement;
import eu.ascetic.paas.applicationmanager.model.Collection;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.applicationmanager.slam.NegotiationWsClient;

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
	public String slaTemplateFile = "ascetic-basic-slat.xml";
	public String slaTemplate;

	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + slaTemplateFile ).toURI());		
		slaTemplate = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}


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

	/**
	 * This test verifies that the PUT request has the right value, accepted field for
	 * the agreement equals to true, if not returns a 400 bad request.
	 */
	@Test
	public void return400IfAcceptedIsNotTrueTest() throws Exception {
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
				+ "<id>33</id>"
				+ "<price>222</price>"
				+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
				+ "<provider-id>provider-id</provider-id>"
				+ "<accepted>false</accepted>"
				+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
				+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
				+ "</agreement>";

		AgreementRest agreementRest = new AgreementRest();
		Response response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);

		assertEquals(400, response.getStatus());
		assertEquals("The only valid change of state it is <accepted>true</accepted>", (String) response.getEntity());
	}

	/**
	 * This test verifies that the right error is raised when trying to accept an agreement that is not in the DB
	 * or it does not belong to the requested deployment
	 */
	@Test
	public void return403IfAgreementNotInDBOrDoesNotBelongToDeploymentTest() throws Exception {
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
				+ "<id>33</id>"
				+ "<price>222</price>"
				+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
				+ "<provider-id>provider-id</provider-id>"
				+ "<accepted>true</accepted>"
				+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
				+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
				+ "</agreement>";

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
		when(deploymentDAO.getById(22)).thenReturn(deployment);

		AgreementRest agreementRest = new AgreementRest();
		agreementRest.deploymentDAO = deploymentDAO;

		Response response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);

		assertEquals(403, response.getStatus());
		assertEquals("Or agreement it is not registered in the Application Manager DB or it does not belong to the requested deployment.", (String) response.getEntity());

		// Also check the same when the list it is empty of null
		deployment = new Deployment();
		deployment.setAgreements(null);

		response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);

		assertEquals(403, response.getStatus());
		assertEquals("Or agreement it is not registered in the Application Manager DB or it does not belong to the requested deployment.", (String) response.getEntity());

		deployment = new Deployment();
		deployment.setAgreements(new ArrayList<Agreement>());

		response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);

		assertEquals(403, response.getStatus());
		assertEquals("Or agreement it is not registered in the Application Manager DB or it does not belong to the requested deployment.", (String) response.getEntity());
	}

	/**
	 * This tests verifies that if there is a previous agreement accepted in the db, the Application Manager will not try to accept
	 * a new one.
	 */
	@Test
	public void return403IfAnAgreementHasBeenAlreadyAccepted() throws Exception {
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
				+ "<id>33</id>"
				+ "<price>222</price>"
				+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
				+ "<provider-id>provider-id</provider-id>"
				+ "<accepted>true</accepted>"
				+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
				+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
				+ "</agreement>";

		Agreement agreement1 = new Agreement();
		agreement1.setAccepted(false);
		agreement1.setId(44);
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id1");
		agreement1.setSlaAgreement("sssas1");
		agreement1.setSlaAgreementId("sla-agreement-id1");
		agreement1.setAccepted(false);

		Agreement agreement2 = new Agreement();
		agreement2.setAccepted(true);
		agreement2.setId(2);
		agreement2.setPrice("222");
		agreement2.setProviderId("provider-id");
		agreement2.setSlaAgreement("sssas");
		agreement2.setSlaAgreementId("sla-agreement-id");
		agreement2.setAccepted(true);

		Deployment deployment = new Deployment();
		deployment.addAgreement(agreement1);
		deployment.addAgreement(agreement2);

		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(22)).thenReturn(deployment);

		AgreementRest agreementRest = new AgreementRest();
		agreementRest.deploymentDAO = deploymentDAO;

		Response response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);

		assertEquals(403, response.getStatus());
		assertEquals("This agreement or a different one from the deployment has been previously accepted.", (String) response.getEntity());
	}

	/** 
	 * This test checks that an agreement can not be accepted if it has already expired.
	 */
	@Test
	public void agreementThatWeAreTryingToAcceptHasExpired() throws Exception {
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
				+ "<id>33</id>"
				+ "<price>222</price>"
				+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
				+ "<provider-id>provider-id</provider-id>"
				+ "<accepted>true</accepted>"
				+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
				+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
				+ "</agreement>";

		Configuration.slaAgreementExpirationTime = "30";

		long expiredTime = System.currentTimeMillis() - ( Long.parseLong(Configuration.slaAgreementExpirationTime) * 60 * 1000) - 1l;

		Agreement agreement1 = new Agreement();
		agreement1.setAccepted(false);
		agreement1.setId(44);
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id1");
		agreement1.setSlaAgreement("sssas1");
		agreement1.setSlaAgreementId("sla-agreement-id1");
		agreement1.setAccepted(false);
		agreement1.setValidUntil(new Timestamp(expiredTime));

		Agreement agreement2 = new Agreement();
		agreement2.setAccepted(true);
		agreement2.setId(2);
		agreement2.setPrice("222");
		agreement2.setProviderId("provider-id");
		agreement2.setSlaAgreement("sssas");
		agreement2.setSlaAgreementId("sla-agreement-id");
		agreement2.setAccepted(false);
		agreement2.setValidUntil(new Timestamp(expiredTime));

		Deployment deployment = new Deployment();
		deployment.addAgreement(agreement1);
		deployment.addAgreement(agreement2);

		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(22)).thenReturn(deployment);

		AgreementRest agreementRest = new AgreementRest();
		agreementRest.deploymentDAO = deploymentDAO;

		Response response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);
		assertEquals(403, response.getStatus());
		assertEquals("The selected agreement has already expired. It is necessary to restart the deployment process.", (String) response.getEntity());
	}

	@Test
	public void signingAnAgreement() throws Exception {
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<agreement xmlns=\"http://application_manager.ascetic.eu/doc/schemas/xml\" href=\"/applications/101/deployments/22/agreements/44\">"
							+ "<id>33</id>"
							+ "<price>222</price>"
							+ "<sla-agreement-id>sla-agreement-id</sla-agreement-id>"
							+ "<provider-id>provider-id</provider-id>"
							+ "<accepted>true</accepted>"
							+ "<link rel=\"deployment\" href=\"/applications/101/deployments/22\" type=\"application/xml\" />"
							+ "<link rel=\"self\" href=\"/applications/101/deployments/22/agreements/44\" type=\"application/xml\" />"
						+ "</agreement>";

		Configuration.slaAgreementExpirationTime = "30";

		long expiredTime = System.currentTimeMillis() + ( Long.parseLong(Configuration.slaAgreementExpirationTime) * 60 * 1000) + 1000000l;

		Agreement agreement1 = new Agreement();
		agreement1.setAccepted(false);
		agreement1.setId(44);
		agreement1.setPrice("111");
		agreement1.setProviderId("provider-id1");
		agreement1.setSlaAgreement(slaTemplate);
		agreement1.setSlaAgreementId("sla-agreement-id1");
		agreement1.setAccepted(false);
		agreement1.setValidUntil(new Timestamp(expiredTime));
		agreement1.setNegotiationId("111");

		Agreement agreement2 = new Agreement();
		agreement2.setAccepted(true);
		agreement2.setId(2);
		agreement2.setPrice("222");
		agreement2.setProviderId("provider-id");
		agreement2.setSlaAgreement("sssas");
		agreement2.setSlaAgreementId("sla-agreement-id");
		agreement2.setAccepted(false);
		agreement2.setValidUntil(new Timestamp(expiredTime));

		Deployment deployment = new Deployment();
		deployment.setId(22);
		deployment.addAgreement(agreement1);
		deployment.addAgreement(agreement2);

		DeploymentDAO deploymentDAO = mock(DeploymentDAO.class);
		when(deploymentDAO.getById(22)).thenReturn(deployment);
		when(deploymentDAO.update(deployment)).thenReturn(true);
		
		NegotiationWsClient negotiationClient = mock(NegotiationWsClient.class);
		SLA sla = new SLA();
		when(negotiationClient.createAgreement(eq(Configuration.slamURL), any(SLATemplate.class), eq(agreement1.getNegotiationId()))).thenReturn(sla);
		
		SLASOIRenderer renderer = mock(SLASOIRenderer.class);
		when(renderer.renderSLA(sla)).thenReturn("sla");
		
		AgreementDAO agreementDAO = mock(AgreementDAO.class);
		when(agreementDAO.update(agreement1)).thenReturn(true);
		
		DeploymentEventService deploymentEventService = mock(DeploymentEventService.class);
		
		AgreementRest agreementRest = new AgreementRest();
		agreementRest.deploymentDAO = deploymentDAO;
		agreementRest.client = negotiationClient;
		agreementRest.rendeder = renderer;
		agreementRest.agreementDAO = agreementDAO;
		agreementRest.deploymentEventService = deploymentEventService; 

		Response response = agreementRest.acceptSlaAgreement("101", "22", "44", payload);
		assertEquals(202, response.getStatus());
		
		String xmlAgreement = (String) response.getEntity();
		Agreement agreement = ModelConverter.xmlAgreementToObject(xmlAgreement);
		assertTrue(agreement.isAccepted());
		assertEquals("sla", agreement1.getSlaAgreement());
		
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, deployment.getStatus());
		
		ArgumentCaptor<DeploymentEvent> argument = ArgumentCaptor.forClass(DeploymentEvent.class);
		verify(deploymentEventService).fireDeploymentEvent(argument.capture());
		DeploymentEvent event = argument.getValue();
		assertEquals(22, event.getDeploymentId());
		assertEquals(Dictionary.APPLICATION_STATUS_CONTEXTUALIZATION, event.getDeploymentStatus());
		assertEquals("101", event.getApplicationName());
		
		// Verification of the different calls
		verify(deploymentDAO).getById(22);
		verify(deploymentDAO).update(deployment);
		verify(agreementDAO).update(agreement1);
	}
	
	@Test
	public void testSlaTemplateIsRight() throws Exception {
		SLASOITemplateParser parser = new SLASOITemplateParser();
		SLATemplate slat = parser.parseTemplate(slaTemplate);
		
		if(slat == null) fail();
	}

	/**
	 * It just reads a file form the disk... 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	protected String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
