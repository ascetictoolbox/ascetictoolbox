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


package eu.ascetic.paas.slam.poc.impl.provider.manager;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;

import com.sun.jersey.api.client.WebResource;

import eu.ascetic.paas.slam.poc.exceptions.SubNegotiationException;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationClient;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationWsClient;
import eu.ascetic.paas.slam.poc.impl.provider.selection.Criterion;



public class ProviderManagerImplRest implements ProviderManager {

	private static ProviderManagerImplRest restManager = null;
	private WebResource resourceRootWeb;
	private String username;
	private final static STND GSLAM_EPR = org.slasoi.slamodel.vocab.sla.gslam_epr;
	private Map<Integer, String> negotiationSessMap;

	
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
	
	

	public ProviderManagerImplRest(){
		System.out.println("Inside ProviderManagerImpl Constructor");
		negotiationSessMap = new HashMap<Integer, String>();
	}

	

	public static ProviderManagerImplRest getInstance(){
		if (restManager==null){
			restManager = new ProviderManagerImplRest();
		}
		return restManager;
	}


		
	private String providerListRequest(String uid, String sid){
		System.out.println("reading provider list from client");
		
		String path = "/users/" + uid + "/slats/" + sid + "/providersList";
		String response = resourceRootWeb.path(path)
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.header("X-Username", username)
			.type(MediaType.APPLICATION_JSON_TYPE)
			.get(String.class);
		System.out.println(response.toString());
		return response.toString();
	}

	
	
	@Override
	public String[] getProvidersList(SLATemplate slat)  {
		try {
			
			String slatId = slat.getUuid().getValue();

			Party[] parties = slat.getParties();
			if (parties == null || parties.length == 0) {
				//throw new Exception("parties can't be null");
				return null;
			}

			String customerId = null;
			for (Party party : parties) {
				STND partyRole = (STND) party.getAgreementRole();
				if (partyRole != null && partyRole.equals(org.slasoi.slamodel.vocab.sla.customer)) {
					customerId = party.getId().getValue();
				}
			}

			if (customerId == null) {
				//throw new Exception("parties can't be null");
				return null;
			}

			JSONObject rJson = new JSONObject(providerListRequest(customerId, slatId));
			
			if (rJson.has("__TITLE__"))System.out.println(rJson.get("__TITLE__"));
			
			if (rJson.has("providersList")){
				JSONArray plist = rJson.getJSONArray("providersList");
				String[] endPoints = new String[plist.length()]; 
				for(int i = 0; i < plist.length(); i++){
					endPoints[i]=(String) plist.get(i);
				}
				return endPoints;
			}
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	@Override
	public SLATemplate[] negotiate(String endPoint, SLATemplate slaTemplate, String IaasRenegotiationUUID) throws SubNegotiationException {

		/**  negotiation code here**/
		
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
			negotiationSessMap.put(slat.toString().hashCode(), negSession);  
		}
		
		return slats;
	}


	public String getNegotiationSession(SLATemplate slat) {
		return negotiationSessMap.get(slat.toString().hashCode()); 
	}
	



	@Override
	public SLA createAgreement(SLATemplate slaTemplate) 
			throws SubNegotiationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Criterion[] getCriteria(SLATemplate slat) {
		// TODO Auto-generated method stub
		return null;
	}

}
