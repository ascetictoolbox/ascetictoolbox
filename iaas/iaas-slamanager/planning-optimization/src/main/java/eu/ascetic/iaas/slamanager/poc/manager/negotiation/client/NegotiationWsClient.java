/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.iaas.slamanager.poc.manager.negotiation.client;

import java.util.ArrayList;
import java.util.List;

import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator.SlaTranslator;
import eu.ascetic.iaas.slamanager.poc.manager.negotiation.translator.SlaTranslatorImpl;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.CreateAgreement;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.CreateAgreementResponse;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.InitiateNegotiationResponse;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.Negotiate;
import eu.ascetic.iaas.slamanager.poc.negotiation.ws.BZNegotiationStub.NegotiateResponse;



public class NegotiationWsClient implements NegotiationClient {

	
	@Override
	public String initiateNegotiation(String endpoint, SLATemplate slaTemplate) {
		
		try {
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			
			InitiateNegotiation doc = getInitiateNegotiationDoc(slaTemplate);
			
			InitiateNegotiationResponse resp = stub.initiateNegotiation(doc);
			
			return resp.get_return();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	@Override
	public SLATemplate[] negotiate(String endpoint, SLATemplate slaTemplate,
			String negotiationId) {
		
		List<SLATemplate> slats = new ArrayList<SLATemplate>(); 
		
		try { 
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			
			Negotiate doc = getNegotiationDoc(negotiationId, slaTemplate);
			
			NegotiateResponse resp = stub.negotiate(doc);
			
			String[] xmlSlats = resp.get_return();
			
			for(String xmlSlat : xmlSlats) {
				SLATemplate slat = (xmlSlat == null) ? null : slaTranslator.parseSlaTemplate(xmlSlat);
				slats.add(slat);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (slats.toArray(new SLATemplate[slats.size()]));
	}
	
	
	

	@Override
	public SLA createAgreement(String endpoint, SLATemplate slaTemplate,
			String negotiationId) {
		
		SLA sla = null;
		try { 
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			
			CreateAgreement doc = getAgreementDoc(negotiationId, slaTemplate);
			
			CreateAgreementResponse resp = stub.createAgreement(doc);
			
			String xmlSla = resp.get_return();
			
			sla = (xmlSla == null) ? null : slaTranslator.parseSla(xmlSla);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sla;
	}
	
	
	
	public InitiateNegotiation getInitiateNegotiationDoc(
			SLATemplate slat) throws java.lang.Exception {

		InitiateNegotiation doc = new InitiateNegotiation();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setSlaTemplate(xmlSlat);
		
		return doc;
	}
	

	
	public Negotiate getNegotiationDoc(String negotiationId,
			SLATemplate slat) throws java.lang.Exception {

		Negotiate doc = new Negotiate();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setNegotiationID(negotiationId);

		doc.setSlaTemplate(xmlSlat);

		return doc;
	}
	
	
	
	public CreateAgreement getAgreementDoc(String negotiationId,
			SLATemplate slat) throws java.lang.Exception {

		CreateAgreement doc = new CreateAgreement();
	
		String xmlSlat = slaTranslator.renderSlaTemplate(slat);
		
		doc.setNegotiationID(negotiationId);

		doc.setSlaTemplate(xmlSlat);

		return doc;
	}
	
	
	public NegotiationWsClient() {
		slaTranslator = new SlaTranslatorImpl();
	}

	
	public void setSlaTranslator(SlaTranslator slaTranslator) {
		this.slaTranslator = slaTranslator;
	}
	
	@Autowired
	private SlaTranslator slaTranslator;

}
