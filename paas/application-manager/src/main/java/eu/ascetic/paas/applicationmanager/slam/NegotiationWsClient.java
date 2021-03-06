package eu.ascetic.paas.applicationmanager.slam;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.slam.sla.model.SLA;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImpl;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreement;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreementResponse;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiationResponse;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Negotiate;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.NegotiateResponse;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Renegotiate;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.RenegotiateResponse;

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
 * ASCETiC implementation of the SLAM Client for the PaaS
 *
 */
public class NegotiationWsClient implements NegotiationClient {
	private static Logger logger = Logger.getLogger(NegotiationWsClient.class);
	private SlaTranslator slaTranslator;
	
	public NegotiationWsClient() {
		slaTranslator = new SlaTranslatorImpl();
	}

	
	public void setSlaTranslator(SlaTranslator slaTranslator) {
		this.slaTranslator = slaTranslator;
	}
	
	private void setTimeout(BZNegotiationStub stub) {
		int timeout = 5 * 60 * 1000; // five minutes

		stub._getServiceClient().getOptions().setProperty(
		                 HTTPConstants.SO_TIMEOUT, new Integer(timeout));
		stub._getServiceClient().getOptions().setProperty(
		                 HTTPConstants.CONNECTION_TIMEOUT, new Integer(timeout));
	}
	
	@Override
	public String initiateNegotiation(String endpoint, SLATemplate slaTemplate) {
		
		try {
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			setTimeout(stub);
			
			InitiateNegotiation doc = getInitiateNegotiationDoc(slaTemplate);
			
			InitiateNegotiationResponse resp = stub.initiateNegotiation(doc);
			
			return resp.get_return();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	@Override
	public String renegotiate(String endpoint, String id) {
		
		try {
			
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			setTimeout(stub);
			
			Renegotiate doc = getRenegotiationDoc(id);
			
			RenegotiateResponse resp = stub.renegotiate(doc);
			
			return resp.get_return();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	@Override
	public SLATemplate[] negotiate(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		
		List<SLATemplate> slats = new ArrayList<SLATemplate>(); 
		
		try { 
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			setTimeout(stub);
			
			Negotiate doc = getNegotiationDoc(negotiationId, slaTemplate);
			
			NegotiateResponse resp = stub.negotiate(doc);
			logger.info("RESPONSE: " + resp);
			
			String[] xmlSlats = resp.get_return();
			logger.info("STATS: " + xmlSlats);
			
			for(String xmlSlat : xmlSlats) {
				logger.info("SLATS: " + xmlSlat);
				SLATemplate slat = (xmlSlat == null) ? null : slaTranslator.parseSlaTemplate(xmlSlat);
				slats.add(slat);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SLATemplate[] templates = slats.toArray(new SLATemplate[slats.size()]);
		logger.info("Templates: " + templates);
		
		return templates;
	}
	
	
	

	@Override
	public SLA createAgreement(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		
		logger.info("Creating agreement");
		logger.info("EndPoint: " + endpoint);
		logger.info("SLA Template: " + slaTemplate);
		logger.info("NegotiationId: " + negotiationId);
		
		SLA sla = null;
		try { 
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			setTimeout(stub);
			
			CreateAgreement doc = getAgreementDoc(negotiationId, slaTemplate);
			
			CreateAgreementResponse resp = stub.createAgreement(doc);
			
			String xmlSla = resp.get_return();
			
			System.out.println("########################################");
			System.out.println(xmlSla);
			
			//sla = (xmlSla == null) ? null : slaTranslator.parseSla(xmlSla);
			//sla = (xmlSla == null) ? null : new SlaTranslatorImpl().parseSla(xmlSla);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(SLA.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			sla = (SLA) jaxbUnmarshaller.unmarshal(new StringReader(xmlSla));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sla;
	}
	
	
	
	public InitiateNegotiation getInitiateNegotiationDoc(SLATemplate slat) throws java.lang.Exception {

		InitiateNegotiation doc = new InitiateNegotiation();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setSlaTemplate(xmlSlat);
		
		return doc;
	}
	
	public Renegotiate getRenegotiationDoc(String id) throws java.lang.Exception {
		Renegotiate doc = new Renegotiate();
		doc.setSlaID(id);
		
		return doc;
	}
	

	
	public Negotiate getNegotiationDoc(String negotiationId, SLATemplate slat) throws java.lang.Exception {

		Negotiate doc = new Negotiate();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setNegotiationID(negotiationId);

		doc.setSlaTemplate(xmlSlat);

		return doc;
	}
	
	
	
	public CreateAgreement getAgreementDoc(String negotiationId, SLATemplate slat) throws java.lang.Exception {

		CreateAgreement doc = new CreateAgreement();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setNegotiationID(negotiationId);

		doc.setSlaTemplate(xmlSlat);

		return doc;
	}
}
