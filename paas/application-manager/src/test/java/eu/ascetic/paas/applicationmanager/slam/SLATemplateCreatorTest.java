package eu.ascetic.paas.applicationmanager.slam;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.testUtil.MockWebServer;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.slaatsoi.slamodel.SLATemplateDocument;

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
 * e-mail david.garciaperez@atos.net 
 * 
 */

public class SLATemplateCreatorTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	private String ovfSelfAdaptationFile = "output-file-ovf-appPackager.ovf";
	private String ovfSelfAdaptationString;
	private MockWebServer mServer;
	private String mBaseURL = "http://localhost:";
	private String value =  "{\"ProvidersList\": [ \n " +
				"{\"provider-uuid\":\"1\", \"p-slam-url\":\"http://10.0.9.149:8080/services/asceticNegotiation?wsdl\"}\n" +
				"{\"provider-uuid\":\"2\", \"p-slam-url\":\"http://10.0.10.149:8080/services/asceticNegotiation?wsdl\"}\n" +
			"]}";
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		file = new File(this.getClass().getResource( "/" + ovfSelfAdaptationFile ).toURI());		
		ovfSelfAdaptationString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		mServer = new MockWebServer();
		mServer.start();
		mBaseURL = "http://localhost:";
		mBaseURL = mBaseURL + mServer.getPort();
	}
	
	@Test
	public void totalConversion() throws Exception {
		// We start fake provider registry
		setupFakeProviderRegistry();
		
		// We read the OVF definition from file
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(ovfSelfAdaptationString);
		
		// We setup the propierties section for the template... /
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition,  "http://localhost/application-manager/appid/deployments/111/ovf");
		
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered =
		SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));
		
		String slaTemplateString = slaTemplateRendered.toString();
		
		eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate slat = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			slat = (eu.ascetic.paas.applicationmanager.slam.sla.model.SLATemplate) jaxbUnmarshaller.unmarshal(new StringReader(slaTemplateString));
		} catch(JAXBException jaxbExpcetion) {
			jaxbExpcetion.printStackTrace();
		}
		
		// We verify that the UUID is correctly set
		assertEquals("ASCETiC-SLaTemplate-Example-01", slat.getUUID());
		assertEquals("sla_at_soi_sla_model_v1.0", slat.getModelVersion());
		
		// We verify that we set correctly the Provider list
		assertEquals(1, slat.getProperties().getEntries().size());
		assertEquals(value, slat.getProperties().getEntries().get(0).getValue());

		System.out.println("SLA rendered as XML: ############################## ");
		System.out.println(slaTemplateString);
	}
	
	private void setupFakeProviderRegistry() {
		// We configure the mocked Provider Registry
		Configuration.providerRegistryEndpoint = mBaseURL + "/";

		String collection = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<collection xmlns=\"http://provider-registry.ascetic.eu/doc/schemas/xml\" href=\"/\">" +
				"<items offset=\"0\" total=\"2\">" +
				"<provider href=\"/1\">" +
				"<id>1</id>" +
				"<name>default</name>" +
				"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
				"<slam-url>http://10.0.9.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
				"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
				"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
				"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
				"</provider>" +
				"<provider href=\"/1\">" +
				"<id>2</id>" +
				"<name>default</name>" +
				"<vmm-url>http://iaas-vm-dev:34372/vmmanager</vmm-url>" +
				"<slam-url>http://10.0.10.149:8080/services/asceticNegotiation?wsdl</slam-url>" +
				"<amqp-url>amqp://guest:guest@iaas-vm-dev:5673</amqp-url>" +
				"<link rel=\"parent\" href=\"/\" type=\"application/xml\"/>" +
				"<link rel=\"self\" href=\"/1\" type=\"application/xml\"/>" +
				"</provider>" +
				"</items>" +
				"<link rel=\"self\" href=\"/\" type=\"application/xml\"/>" +
				"</collection>";

		mServer.addPath("/", collection);
	}
	
	@Test
	public void addPropertiesTest() {
		setupFakeProviderRegistry();
		
		// Test
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addProperties(slaTemplate);
		
		STND[] propertiesKeys = slaTemplate.getPropertyKeys();
		assertEquals(1, propertiesKeys.length);
		assertEquals(value, slaTemplate.getPropertyValue(propertiesKeys[0])); 
	}
	
	@Test
	public void verifyPartyIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		
		assertEquals("AsceticProvider", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		
		SLATemplateCreator.addProviderEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyCostumerIsConfiguredCorrectly() throws Exception {
		SLATemplate slaTemplate = new SLATemplate();
		
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		//"ASCETiCUser"
		assertEquals("333", slaTemplate.getParties()[0].getId().getValue());
		assertEquals("http://www.slaatsoi.org/slamodel#gslam_epr", slaTemplate.getParties()[0].getPropertyKeys()[0].getValue());
		assertEquals(Configuration.slamURL, slaTemplate.getParties()[0].getPropertyValue(slaTemplate.getParties()[0].getPropertyKeys()[0]));
		
		System.out.println(slaTemplate);
		
		// Check it does not overwrite all the parties:
		SLATemplateCreator.addUserEndPointToTemplate(slaTemplate);
		assertEquals(2, slaTemplate.getParties().length);
	}
	
	@Test
	public void verifyAddInterfaceDclr() {
		SLATemplate slaTemplate = new SLATemplate();
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		
		SLATemplateCreator.addInterfaceDclr(slaTemplate, ovfDefinition, "OVF_URL");
		
		// TODO after I know what I'm doing creating the template
		
		System.out.println(slaTemplate);
		
	}
	
	@Test
	public void verifyGenerateSLATemplate() throws Exception {
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);

		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "http://10.4.0.16/application-manager/applications/threeTierWebApp/deployments/31/ovf");

		assertEquals("ASCETiC-SLaTemplate-Example-01", slaTemplate.getUuid().getValue());
		//assertEquals("1", slaTemplate.getModelVersion());

		System.out.println(slaTemplate);

		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered =
		SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplate));

		System.out.println("SLA rendered as XML:");
		System.out.println(slaTemplateRendered.toString());
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
