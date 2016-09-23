package eu.ascetic.paas.applicationmanager.slam;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.syntaxconverter.SyntaxConverterDelegator;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.utils.ovf.api.OvfDefinition;

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
 * Unit test that verifies the correct work of the SLAM Client
 * 
 */

public class SLAMClientTest {
	private String threeTierWebAppOvfFile = "3tier-webapp.ovf.xml";
	private String threeTierWebAppOvfString;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + threeTierWebAppOvfFile ).toURI());		
		threeTierWebAppOvfString = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}

	//@Test
	public void getInitiateNegotiationDocumentTest() throws Exception {
		//We create an SLA Document from a valid OVF file
		OvfDefinition ovfDefinition = OVFUtils.getOvfDefinition(threeTierWebAppOvfString);
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "REST-URL", null);
		
		InitiateNegotiation inParam = new InitiateNegotiation();
		SyntaxConverterDelegator delegator = new SyntaxConverterDelegator(SyntaxConverterType.SLASOISyntaxConverter);
		inParam.setSlaTemplate(delegator.renderSLATemplate(slaTemplate));

		
		System.out.println(inParam.getSlaTemplate());
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
