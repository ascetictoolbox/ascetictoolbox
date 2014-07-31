package eu.ascetic.iaas.slamanager.poc.negotiation.ws;

import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slasoi.gslam.syntaxconverter.SLASOITemplateRenderer;
import org.slasoi.gslam.syntaxconverter.SLATemplateRenderer;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.w3c.dom.CDATASection;

public class NegotiationWsClientRaw implements NegotiationClient {

	public NegotiationWsClientRaw() {

	}

	@Override
	public SLATemplate[] negotiate(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String initiateNegotiation(String endpoint, SLATemplate slaTemplate) {
		SOAPMessage req = null;
		try {
			req = createInitiateNegotiationReq(slaTemplate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SOAPMessage resp = sendSoapRequest(endpoint, req);
		return resp.getContentDescription();
	}

	@Override
	public SLA createAgreement(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		// TODO Auto-generated method stub
		return null;
	}

	public static SOAPMessage createInitiateNegotiationReq(SLATemplate slat) throws java.lang.Exception {

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(WEB_NS, nameSpaces.get(WEB_NS));

		SOAPBody soapBody = envelope.getBody();

		SOAPElement initiateElem = soapBody.addChildElement("initiateNegotiation", "web");
		SOAPElement slatElem = initiateElem.addChildElement("slaTemplate");

		SLATemplateRenderer renderer = new SLASOITemplateRenderer();
		String xmlSlat = renderer.renderSLATemplate(slat);
		CDATASection cdata = soapMessage.getSOAPPart().createCDATASection(xmlSlat);
		slatElem.appendChild(cdata);

		System.out.print("Request SOAP Message = ");
		soapMessage.writeTo(System.out);
		System.out.println();

		return soapMessage;
	}

	public SOAPMessage sendSoapRequest(String url, SOAPMessage message) {

		SOAPMessage soapResponse = null;

		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();

			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			soapResponse = soapConnection.call(message, url);

			soapConnection.close();

		} catch (java.lang.Exception e) {
			System.err.println("Error while sending SOAP Request:");
			e.printStackTrace();
		}
		return soapResponse;
	}

	private static final String WEB_NS = "web";

	private static final Map<String, String> nameSpaces = new HashMap<String, String>() {
		{
			put(WEB_NS, "http://webservice.syntaxconverter.gslam.slasoi.org");
		}
	};

}