package eu.ascetic.paas.applicationmanager.slam.sla;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
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
 * Test the correct work of the SLA Agreement Helper class
 *
 */
public class SLAAgreementHelperTest {
	private String slaAgreement2File = "sla-agreement-2.xml";
	private String slaAgreement2Text;
	private String slaAgreementFile = "sla-agreement.xml";
	private String slaAgreementText;

	
	/**
	 * We just read an sla agreement example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + slaAgreement2File ).toURI());		
		slaAgreement2Text = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
		
		file = new File(this.getClass().getResource( "/" + slaAgreementFile ).toURI());		
		slaAgreementText = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void constructor() {
		SLAAgreementHelper helper = new SLAAgreementHelper(slaAgreement2Text);
		assertNotNull(helper.getSla());
		assertEquals("1a9a2a46-cfe5-477c-8508-94417955ab53", helper.getSla().getUuid());
		
		helper = new SLAAgreementHelper("random text...");
		assertNull(helper.getSla());
	}
	
	@Test
	public void getPowerUsagePerApp() {
		SLAAgreementHelper helper = new SLAAgreementHelper(slaAgreement2Text);
		double powerUsage = helper.getPowerUsagePerApp();
		
		assertEquals(21.0, powerUsage, 0.0001);
		
		helper = new SLAAgreementHelper(slaAgreementText);
		powerUsage = helper.getPowerUsagePerApp();
		
		assertEquals(0.0, powerUsage, 0.0001);
	}
	
	@Test
	public void getPowerUsagePerAppUnits() {
		SLAAgreementHelper helper = new SLAAgreementHelper(slaAgreement2Text);
		String units = helper.getPowerUsagePerAppUnits();
		
		assertEquals("Watt", units);
		
		helper = new SLAAgreementHelper(slaAgreementText);
		units = helper.getPowerUsagePerAppUnits();
		
		assertNull(units);
	}
	
	@Test
	public void getPowerUnitsPerOVFId() {
		SLAAgreementHelper helper = new SLAAgreementHelper(slaAgreement2Text);
	
		String units = helper.getPowerUnitsPerOVFId("jboss");
		assertEquals("Watt", units);
		
		helper = new SLAAgreementHelper(slaAgreementText);
		
		units = helper.getPowerUnitsPerOVFId("jboss");
		assertNull(units);
	}
	
	@Test
	public void getPowerUsagePerOVFId() {
		SLAAgreementHelper helper = new SLAAgreementHelper(slaAgreement2Text);
	
		double powerUsage = helper.getPowerUsagePerOVFId("jboss");
		assertEquals(10.0, powerUsage, 0.0001);
		
		powerUsage = helper.getPowerUsagePerOVFId("haproxy");
		assertEquals(12.3, powerUsage, 0.0001);
		
		helper = new SLAAgreementHelper(slaAgreementText);
		
		powerUsage = helper.getPowerUsagePerOVFId("jboss");
		assertEquals(0.0, powerUsage, 0.0001);
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
