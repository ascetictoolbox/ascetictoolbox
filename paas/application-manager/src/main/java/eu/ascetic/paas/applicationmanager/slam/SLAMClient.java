package eu.ascetic.paas.applicationmanager.slam;

import org.slasoi.gslam.syntaxconverter.webservice.InitiateNegotiationDocument;
import org.slasoi.gslam.syntaxconverter.webservice.impl.InitiateNegotiationDocumentImpl;

import eu.slaatsoi.slamodel.SLATemplateDocument.SLATemplate;

/**
 * Application Manager client to the ASCETiC PaaS SLA Manager server
 * @author David Garcia Perez - Atos
 *
 */
public class SLAMClient {

	/**
	 * It creates from an SLA Template the Initial negotation document to send to the PaaS SLAM
	 * @param slaTemplateDocument initial document
	 * @return the Axis2 InitiateNegotiationDocument
	 */
	protected InitiateNegotiationDocument getInitiateNegotiationDocument(SLATemplate slaTemplateDocument) {
		
		InitiateNegotiationDocumentImpl initiateNegotiationDocument = new InitiateNegotiationDocumentImpl(slaTemplateDocument.schemaType());
		//initiateNegotiationDocument.setS
		
		return null;
	}
}
