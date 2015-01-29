package eu.ascetic.paas.applicationmanager.slam;

import java.util.ArrayList;
import java.util.List;

import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslator;
import eu.ascetic.paas.applicationmanager.slam.translator.SlaTranslatorImpl;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreement;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.CreateAgreementResponse;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiation;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.InitiateNegotiationResponse;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.Negotiate;
import eu.ascetic.applicationmanager.slam.stub.BZNegotiationStub.NegotiateResponse;

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
	private SlaTranslator slaTranslator;
	
	public NegotiationWsClient() {
		slaTranslator = new SlaTranslatorImpl();
	}

	
	public void setSlaTranslator(SlaTranslator slaTranslator) {
		this.slaTranslator = slaTranslator;
	}
	
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
	public SLATemplate[] negotiate(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		
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
	public SLA createAgreement(String endpoint, SLATemplate slaTemplate, String negotiationId) {
		
		SLA sla = null;
		try { 
			BZNegotiationStub stub = new BZNegotiationStub(endpoint);
			
			CreateAgreement doc = getAgreementDoc(negotiationId, slaTemplate);
			
			CreateAgreementResponse resp = stub.createAgreement(doc);
			
			String xmlSla = resp.get_return();
			
			System.out.println("########################################");
			System.out.println(xmlSla);
			
			sla = (xmlSla == null) ? null : slaTranslator.parseSla(xmlSla);
			
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
