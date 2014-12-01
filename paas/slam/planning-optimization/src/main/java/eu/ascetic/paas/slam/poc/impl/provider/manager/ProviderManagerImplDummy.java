/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.ascetic.paas.slam.poc.impl.provider.manager;

import java.util.HashMap;
import java.util.Map;

import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.SubNegotiationException;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationClient;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationWsClient;

public class ProviderManagerImplDummy implements ProviderManager {

	
	private final static STND GSLAM_EPR = org.slasoi.slamodel.vocab.sla.gslam_epr;
	
	private Map<Integer, NegotiationInfo> negotiationSessMap;
	
	
	
	private class NegotiationInfo {
		
		private String negotiationId;
		private String endPoint;
		private Map<String, String> providersMap;
		
		public NegotiationInfo() {
			providersMap = new HashMap<String, String>();
		}

		public String getNegotiationId() {
			return negotiationId;
		}
		
		public void setNegotiationId(String negotiationId) {
			this.negotiationId = negotiationId;
		}
		
		public String getEndPoint() {
			return endPoint;
		}
		
		public void setEndPoint(String endPoint) {
			this.endPoint = endPoint;
		}
		
	
	}
	
	
	
	public ProviderManagerImplDummy() {
		negotiationSessMap = new HashMap<Integer, NegotiationInfo>();
	}

	
	
	@Override
	public String[] getProvidersList(SLATemplate slat) {
		String[] endPoints = new String[1];
		endPoints[0] = "http://10.15.5.52:8080/services/asceticNegotiation?wsdl";
		return endPoints;
	}

	
	
	@Override
	public SLATemplate[] negotiate(String endPoint, SLATemplate slaTemplate) throws SubNegotiationException {
		NegotiationClient nc = new NegotiationWsClient();
		
		Party providerParty = getProviderParty(slaTemplate); 
		String fedEndPoint = providerParty.getPropertyValue(GSLAM_EPR);
		
		/* configures SLAT for the provider endpoint */
		providerParty.setPropertyValue(GSLAM_EPR, endPoint);
		
		String negSession = nc.initiateNegotiation(endPoint, slaTemplate);
		SLATemplate[] slats = nc.negotiate(endPoint, slaTemplate, negSession);

		for (SLATemplate slat : slats) {
			Party retParty = getProviderParty(slat);
			/* reconfigures SLAT for the PaaS endpoint */
			retParty.setPropertyValue(GSLAM_EPR,  fedEndPoint);
			NegotiationInfo ni = new NegotiationInfo();
			ni.setEndPoint(endPoint);
			ni.setNegotiationId(negSession);
			negotiationSessMap.put(slat.toString().hashCode(), ni);  
		}
		
		return slats;
	}

	
	
	@Override
	public SLA createAgreement(SLATemplate slaTemplate)
			throws SubNegotiationException {
		
		NegotiationClient nc = new NegotiationWsClient();
		Party providerParty = getProviderParty(slaTemplate); 
		
		String fedEndPoint = providerParty.getPropertyValue(GSLAM_EPR);
		
		Integer slatKey = slaTemplate.toString().hashCode();
		
		NegotiationInfo ni = negotiationSessMap.get(slatKey);
		
		if (ni == null) {
			throw new SubNegotiationException("Could not retrieve negotiation SLAT");
		}
		
		String provEndPoint = ni.getEndPoint();
		/* configures SLA for the provider endpoint */
		providerParty.setPropertyValue(GSLAM_EPR, provEndPoint);
		
		SLA sla = nc.createAgreement(provEndPoint, slaTemplate, ni.getNegotiationId());

		Party retParty = getProviderParty(sla);
		/* reconfigures SLA for the PaaS endpoint */
		retParty.setPropertyValue(GSLAM_EPR,  fedEndPoint);
		
		negotiationSessMap.remove(slatKey);
		
		return sla;
	}
	
	

	
	private static Party getProviderParty(SLATemplate slaTemplate) throws SubNegotiationException {
		Party[] parties = slaTemplate.getParties();
		for (Party party : parties) {
			STND partyRole = (STND) party.getAgreementRole();
			if (partyRole != null && partyRole.equals(org.slasoi.slamodel.vocab.sla.provider)) {
				if (party.getPropertyValue(GSLAM_EPR) == null)
					throw new SubNegotiationException("Could not find Provider EPR");
				return party;
			}
		}
		return null;
	}



}
