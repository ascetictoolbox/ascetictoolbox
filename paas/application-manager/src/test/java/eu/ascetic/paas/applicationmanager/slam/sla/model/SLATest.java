package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;

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
 * This class is the Unit test that verifies the correct parsing of the SLA Agreement
 */

public class SLATest {
	private String slaTemplateFile = "sla-agreement.xml";
	private String slaAgreementFile = "test-sla.xml";
	private String slaTemplateString;
	private String slaAgreementString;

	/**
	 * We just read an SLA agreement example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + slaTemplateFile ).toURI());		
		slaTemplateString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		file = new File(this.getClass().getResource( "/" + slaAgreementFile ).toURI());		
		slaAgreementString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void pojo() {
		SLA sla = new SLA();
		List<AgreementTerm> agreementTerms = new ArrayList<AgreementTerm>();
		sla.setAgreementTerms(agreementTerms);
		sla.setUuid("uuid");
		
		assertEquals(agreementTerms, sla.getAgreementTerms());
		assertEquals("uuid", sla.getUuid());
	}
	
	@Test
	public void testUUID() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(SLA.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		SLA slaAgreement = (SLA) jaxbUnmarshaller.unmarshal(new StringReader(slaAgreementString));
		
		assertEquals("95f718dd-3665-461a-b6c2-e89a0f98c473", slaAgreement.getUuid());
	}
	
	@Test
	public void getSlaTest() throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(SLA.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		SLA slaAgreement = (SLA) jaxbUnmarshaller.unmarshal(new StringReader(slaTemplateString));
		
		assertNotNull(slaAgreement);
		assertEquals(8, slaAgreement.getAgreementTerms().size());
		
		AgreementTerm agreementTerm1 = slaAgreement.getAgreementTerms().get(0);
		assertEquals("jmeter_Guarantees", agreementTerm1.getId());
		
		AgreementTerm agreementTerm2 = slaAgreement.getAgreementTerms().get(1);
		assertEquals("haproxy_Guarantees", agreementTerm2.getId());
		
		AgreementTerm agreementTerm3 = slaAgreement.getAgreementTerms().get(2);
		assertEquals("jboss_Guarantees", agreementTerm3.getId());
		
		AgreementTerm agreementTerm4 = slaAgreement.getAgreementTerms().get(3);
		assertEquals("mysql_Guarantees", agreementTerm4.getId());
		
		AgreementTerm agreementTerm5 = slaAgreement.getAgreementTerms().get(4);
		assertEquals("Infrastructure_Price_Of_jmeter", agreementTerm5.getId());
		assertNotNull(agreementTerm5.getGuaranteed());
		assertNotNull(agreementTerm5.getGuaranteed().getAction());
		assertNotNull(agreementTerm5.getGuaranteed().getAction().getPostCondition());
		assertNotNull(agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice());
		assertEquals("Product_Offering_Price_Of_jmeter", agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getId());
		assertNotNull(agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice());
		assertNotNull(agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity());
		assertEquals(0.0, agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#http://www.slaatsoi.org/coremodel/units#EUR", 
				     agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getDatatype());
		assertEquals(1l,agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#vm", 
				     agreementTerm5.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getDatatype());
		
		AgreementTerm agreementTerm6 = slaAgreement.getAgreementTerms().get(5);
		assertEquals("Infrastructure_Price_Of_haproxy", agreementTerm6.getId());
		assertNotNull(agreementTerm6.getGuaranteed());
		assertNotNull(agreementTerm6.getGuaranteed().getAction());
		assertNotNull(agreementTerm6.getGuaranteed().getAction().getPostCondition());
		assertNotNull(agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice());
		assertEquals("Product_Offering_Price_Of_haproxy", agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getId());
		assertNotNull(agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice());
		assertNotNull(agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity());
		assertEquals(0.0, agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#http://www.slaatsoi.org/coremodel/units#EUR", 
				     agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getDatatype());
		assertEquals(1l,agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#vm", 
				     agreementTerm6.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getDatatype());
		
		AgreementTerm agreementTerm7 = slaAgreement.getAgreementTerms().get(6);
		assertEquals("Infrastructure_Price_Of_jboss", agreementTerm7.getId());
		assertNotNull(agreementTerm7.getGuaranteed());
		assertNotNull(agreementTerm7.getGuaranteed().getAction());
		assertNotNull(agreementTerm7.getGuaranteed().getAction().getPostCondition());
		assertNotNull(agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice());
		assertEquals("Product_Offering_Price_Of_jboss", agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getId());
		assertNotNull(agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice());
		assertNotNull(agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity());
		assertEquals(0.0, agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#http://www.slaatsoi.org/coremodel/units#EUR", 
				     agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getDatatype());
		assertEquals(1l,agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#vm", 
				     agreementTerm7.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getDatatype());
		
		AgreementTerm agreementTerm8 = slaAgreement.getAgreementTerms().get(7);
		assertEquals("Infrastructure_Price_Of_mysql", agreementTerm8.getId());
		assertNotNull(agreementTerm8.getGuaranteed());
		assertNotNull(agreementTerm8.getGuaranteed().getAction());
		assertNotNull(agreementTerm8.getGuaranteed().getAction().getPostCondition());
		assertNotNull(agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice());
		assertEquals("Product_Offering_Price_Of_mysql", agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getId());
		assertNotNull(agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice());
		assertNotNull(agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity());
		assertEquals(0.0, agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#http://www.slaatsoi.org/coremodel/units#EUR", 
				     agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getPrice().getDatatype());
		assertEquals(1l,agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getValue(), 0.0001);
		assertEquals("http://www.slaatsoi.org/coremodel/units#vm", 
				     agreementTerm8.getGuaranteed().getAction().getPostCondition().getProductOfferingPrice().getComponentProdOfferingPrice().getQuantity().getDatatype());
		
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
