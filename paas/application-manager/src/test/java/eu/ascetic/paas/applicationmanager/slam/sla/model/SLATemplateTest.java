package eu.ascetic.paas.applicationmanager.slam.sla.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
 * JUnit test to verify the behaviour of the SLATemplate test.
 */
public class SLATemplateTest {

	@Test
	public void pojo() {
		SLATemplate slat = new SLATemplate();
		slat.setUUID("uuid");
		slat.setModelVersion("model");
		Properties properties = new Properties();
		slat.setProperties(properties);
		List<Party> parties = new ArrayList<Party>();
		slat.setParties(parties);
		List<InterfaceDeclr> interfaceDeclrs = new ArrayList<InterfaceDeclr>();
		slat.setInterfaceDeclrs(interfaceDeclrs);
		List<AgreementTerm> agreementTerms = new ArrayList<AgreementTerm>();
		slat.setAgreemenTerms(agreementTerms);
		
		assertEquals(agreementTerms, slat.getAgreemenTerms());
		assertEquals(interfaceDeclrs, slat.getInterfaceDeclrs());
		assertEquals("uuid", slat.getUUID());
		assertEquals("model", slat.getModelVersion());
		assertEquals(properties, slat.getProperties());
		assertEquals(parties, slat.getParties());
	}
}