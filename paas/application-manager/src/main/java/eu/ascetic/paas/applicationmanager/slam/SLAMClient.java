package eu.ascetic.paas.applicationmanager.slam;

import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.webservice.InitiateNegotiationDocument;
import org.slasoi.gslam.syntaxconverter.webservice.impl.InitiateNegotiationDocumentImpl;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.slaatsoi.slamodel.SLATemplateDocument;

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
	protected InitiateNegotiationDocument getInitiateNegotiationDocument(SLATemplate slaTemplateDocument) throws Exception {
		
		// We convert the SLATemplate to String:
		SLASOITemplateRenderer slasoiTemplateRenderer = new SLASOITemplateRenderer();
		SLATemplateDocument slaTemplateRendered = SLATemplateDocument.Factory.parse(slasoiTemplateRenderer.renderSLATemplate(slaTemplateDocument));
		
		InitiateNegotiationDocument initiateNegotiationDocument = InitiateNegotiationDocument.Factory.newInstance();
	//	InitiateNegotiationDocument.InitiateNegotiation initiateNegotiation = initiateNegotiationDocument.addNewInitiateNegotiation();
//		initiate.addNewInitiateNegotiation();
//		initiate.getInitiateNegotiation().setSlaTemplate(slaTemplateRendered.toString());
		//InitiateNegotiationDocumentImpl initiateNegotiationDocument =
//		initiateNegotiationDocument.addNewInitiateNegotiation();
//		initiateNegotiationDocument.getInitiateNegotiation().setSlaTemplate(slaTemplateRendered.toString());
		
		return null;
	}
}
