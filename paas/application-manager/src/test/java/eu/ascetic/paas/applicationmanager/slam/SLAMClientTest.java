package eu.ascetic.paas.applicationmanager.slam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.core.negotiation.ISyntaxConverter.SyntaxConverterType;
import org.slasoi.gslam.syntaxconverter.SyntaxConverterDelegator;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.paas.applicationmanager.ovf.OVFUtils;
import eu.ascetic.utils.ovf.api.OvfDefinition;

/**
 * Unit test that verifies the correct work of the SLAM Client
 * @author David Garcia Perez - Atos
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
		SLATemplate slaTemplate = SLATemplateCreator.generateSLATemplate(ovfDefinition, "REST-URL");
		
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
