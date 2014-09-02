package eu.ascetic.paas.applicationmanager.slam;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slasoi.gslam.syntaxconverter.SLASOIRenderer;
import org.slasoi.gslam.syntaxconverter.SLASOITemplateParser;
import org.slasoi.slamodel.sla.SLATemplate;

public class PlayingTestIT {
	private String iaaSRequestSLATemplate = "a.xml";
	private String proposalTemplate;
	
	/**
	 * We just read an ovf example... 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Before
	public void setup() throws IOException, URISyntaxException {
		File file = new File(this.getClass().getResource( "/" + iaaSRequestSLATemplate ).toURI());		
		proposalTemplate = readFile(file.getAbsolutePath(), StandardCharsets.UTF_8);
	}
	
	@Test
	public void test() throws Exception {
		SLASOITemplateParser tp = new SLASOITemplateParser();

		SLATemplate slatProposal = tp.parseTemplate(proposalTemplate);
		System.out.println("UUID: " + slatProposal.getUuid());
		
		System.out.println(slatProposal.toString());
		//
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
