/**
 * Copyright 2012 Hewlett-Packard Development Company, L.P.                
 *                                                                          
 * Licensed under the Apache License, Version 2.0 (the "License");         
 * you may not use this file except in compliance with the License.        
 * You may obtain a copy of the License at                                 
 *                                                                          
 *     http://www.apache.org/licenses/LICENSE-2.0                          
 *                                                                          
 * Unless required by applicable law or agreed to in writing, software     
 * distributed under the License is distributed on an "AS IS" BASIS,       
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and     
 * limitations under the License.                                          
 */


package eu.ascetic.paas.slam.poc.impl.provider.manager;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slasoi.gslam.core.negotiation.SLARegistry;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.primitives.UUID;
import org.slasoi.slamodel.sla.InterfaceDeclr;
import org.slasoi.slamodel.sla.Party;
import org.slasoi.slamodel.sla.SLA;
import org.slasoi.slamodel.sla.SLATemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eu.ascetic.paas.slam.poc.exceptions.SubNegotiationException;
import eu.ascetic.paas.slam.poc.impl.config.ConfigManager;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationClient;
import eu.ascetic.paas.slam.poc.impl.provider.negotiation.NegotiationWsClient;
import eu.ascetic.paas.slam.poc.impl.provider.selection.Criterion;
import eu.ascetic.paas.slam.poc.impl.slaparser.SlaTemplateEntitiesParser;



public class ProviderManagerImpl implements ProviderManager {

	
	private final static STND GSLAM_EPR = org.slasoi.slamodel.vocab.sla.gslam_epr;
	
	private Map<Integer, NegotiationInfo> negotiationSessMap;
	
	private static final Logger LOGGER = Logger.getLogger(ProviderManagerImpl.class);
	
	
	public ProviderManagerImpl() {
		negotiationSessMap = new HashMap<Integer, NegotiationInfo>();
	}

	
	
	/*
	 * returns the list of provider endpoints
	 */
	@Override
	public String[] getProvidersList(SLATemplate slat)  {
		ArrayList<String> endpoints = new ArrayList<String>();
		try {
			ConfigManager cm = ConfigManager.getInstance();
//			HttpClient client = HttpClientBuilder.create().build();
			HttpClient client = new DefaultHttpClient();
			
			//provider registry endpoint taken from configuration file
			HttpGet request = new HttpGet(cm.getRegistryEndpoint());
			
			HttpResponse response = client.execute(request);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new InputStreamReader(response.getEntity().getContent()));
			Document d = builder.parse(is);

			if (d!=null) {
				NodeList nl = d.getElementsByTagName("slam-url");
				for(int i=0; i<nl.getLength(); i++){
					Node childNode = nl.item(i);
					LOGGER.debug("Found endpoint "+childNode.getTextContent());
					endpoints.add(childNode.getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Object[] endpointsArray = endpoints.toArray();		
		return Arrays.asList(endpointsArray).toArray(new String[endpointsArray.length]);
	}
	
	
	
	
	@Override
	public SLATemplate[] negotiate(String provEndpoint, 
			SLATemplate slaTemplate, String IaaSrenegotiationUUID) throws SubNegotiationException {
		
		Party slaTemplateParty = getProviderParty(slaTemplate); 
		String fedEndpoint = slaTemplateParty.getPropertyValue(GSLAM_EPR);
		
		String providerUiid = getProviderUuid(slaTemplate, provEndpoint);
		
		// change provider endpoint in the original SLAT 
		slaTemplateParty.setPropertyValue(GSLAM_EPR, provEndpoint);

		NegotiationClient nc = new NegotiationWsClient();
		String negSession = "";
		
		/*
		 * GESTIONE RINEGOZIAZIONE
		 */
		if (IaaSrenegotiationUUID==null) {
			LOGGER.debug("Invoking initiateNegotiation at IaaS level");
			negSession = nc.initiateNegotiation(provEndpoint, slaTemplate);
		}
		else {
			LOGGER.debug("Invoking renegotiation at IaaS level");
			negSession = nc.renegotiate(provEndpoint, IaaSrenegotiationUUID);
		}
		/*
		 * FINE GESTIONE RINEGOZIAZIONE
		 */
		
		SLATemplate[] retSlats = nc.negotiate(provEndpoint, slaTemplate, negSession);

		for (SLATemplate retSlat : retSlats) {
			// rewrite the PaaS endpoint
			Party retParty = getProviderParty(retSlat);
			retParty.setPropertyValue(GSLAM_EPR, fedEndpoint);
			
			// set JSON info useful for PaaS core
			setProvidersList(retSlat, providerUiid, provEndpoint);
			
			// store info to be used at createAgreement time
			NegotiationInfo ni = new NegotiationInfo();
			ni.setEndPoint(provEndpoint);
			ni.setNegotiationId(negSession);
			ni.setSlaTemplate(retSlat.toString());
			negotiationSessMap.put(getProviderUuid(retSlat, null).hashCode(), ni);
		}

		// resume original PaaS endpoint in the original SLAT 
		slaTemplateParty.setPropertyValue(GSLAM_EPR, fedEndpoint);
		
		return retSlats;
	}

	
	
	@Override
	public SLA createAgreement(SLATemplate slaTemplate)
			throws SubNegotiationException {
		
		Party providerParty = getProviderParty(slaTemplate); 
		String fedEndpoint = providerParty.getPropertyValue(GSLAM_EPR);

		String provId = getProviderUuid(slaTemplate, null);
		LOGGER.debug("Looking for offer: " + slaTemplate.toString());
		LOGGER.debug("Provider " + provId);
		
		Integer slatKey = provId.hashCode();
		NegotiationInfo ni = negotiationSessMap.get(slatKey);
		
		if (ni == null) {
			LOGGER.error("No offers found for provider " + provId);
			throw new SubNegotiationException("Could not retrieve negotiation SLAT");
		}
		
		if (!ni.getSlaTemplate().equals(slaTemplate.toString())) {
			LOGGER.error("Input SLA template does not match the original offer");
			throw new SubNegotiationException("Invalid input SLA template");
		}

		String providerUiid = getProviderUuid(slaTemplate, ni.getEndPoint());
		
		String provEndpoint = ni.getEndPoint();
		
		// configure SLAT with the provider endpoint 
		providerParty.setPropertyValue(GSLAM_EPR, provEndpoint);

		NegotiationClient nc = new NegotiationWsClient();
		
		// set ProviderUiid for enforcement at provider level
		slaTemplate.setPropertyValue(new STND("ProviderUUid"), providerUiid);
		
		SLA sla = nc.createAgreement(provEndpoint, slaTemplate, ni.getNegotiationId());

		String providerSlaId = sla.getUuid().getValue();

		// reconfigures SLA for the PaaS endpoint 
		Party retParty = getProviderParty(sla);
		retParty.setPropertyValue(GSLAM_EPR,  fedEndpoint);
		
		// set JSON info useful for PaaS core
		setProvidersInfoInSLA(sla, providerUiid, providerSlaId);
		
		negotiationSessMap.remove(slatKey);
		
		return sla;
	}
	
	

	private static Party getProviderParty(SLATemplate slaTemplate) throws SubNegotiationException {
		Party party = SlaTemplateEntitiesParser.getProviderParty(slaTemplate);
		if (party == null) {
			throw new SubNegotiationException("Could not find Provider party");
		}
		return party;
	}



	private static Double getFrac(String candidate) {
		Double d;
		try {
		 d = Double.parseDouble(candidate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (d < 0 || d > 1) 
			return null;
		
		return d;
	}
	
	
	
	public static String getProviderUuid(SLATemplate slat, String endpoint) {
		
		STND[] rootPropKeys = slat.getPropertyKeys();
		if (null == rootPropKeys || rootPropKeys.length == 0)
			return null;

		for (int i = 0; i < rootPropKeys.length; i++) {
			if (rootPropKeys[i].equals("ProvidersList")) {
				String providersValue = slat.getPropertyValue(rootPropKeys[i]);
				
				try {
					JSONObject json = new JSONObject(providersValue);
					JSONArray provList = json.getJSONArray("ProvidersList");
					
					if (endpoint == null) {
						return (String) provList.getJSONObject(0).get("provider-uuid");
					}
					
					for (int j = 0; j < provList.length(); j++) {
						JSONObject json_object = provList.getJSONObject(j);
						if (((String) json_object.get("p-slam-url")).equals(endpoint)) {
							return (String) json_object.get("provider-uuid");
						}
				    }
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;	

	}
	
	
	
	public void setProvidersList(SLATemplate slat, String providerId, String providerUrl) 
			throws SubNegotiationException {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("ProvidersList", new JSONArray());
			JSONObject jsonProvider = new JSONObject();
			jsonProvider.put("provider-uuid", providerId);
			jsonProvider.put("p-slam-url", providerUrl);
			jsonObj.accumulate("ProvidersList", jsonProvider);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new SubNegotiationException("Could not set ProviderList in SLAT offer");
		}
		slat.setPropertyValue(new STND("ProvidersList"), jsonObj.toString());
	}
	
	
	
	public void setProvidersInfoInSLA(SLA sla, String providerId, String providerSlaId) 
			throws SubNegotiationException {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("SLA-ProvidersList", new JSONArray());
			JSONObject jsonProvSla = new JSONObject();
			jsonProvSla.put("provider-uuid", providerId);
			jsonProvSla.put("sla-id", providerSlaId);
			jsonObj.accumulate("SLA-ProvidersList", jsonProvSla);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new SubNegotiationException("Could not set ProviderList in SLAT offer");
		}
		
		InterfaceDeclr[] interfaces = sla.getInterfaceDeclrs();
		for (InterfaceDeclr intrf : interfaces) {
			intrf.setPropertyValue(new STND("SLA-ProvidersList"), jsonObj.toString());
		}
		
	}
	
	@Override
	public Criterion[] getCriteria(SLATemplate slat) {
		
		List<Criterion> criteria = new ArrayList<Criterion>();
		
		STND[] rootPropKeys = slat.getPropertyKeys();
		if (rootPropKeys == null || rootPropKeys.length == 0)
			return null;
					
		for (int i = 0; i < rootPropKeys.length; i++) {
			if (rootPropKeys[i].equals("Criteria")) {
				String criteriaValue = slat.getPropertyValue(rootPropKeys[i]);
				
				JSONObject json;
				try {
					json = new JSONObject(criteriaValue);
					JSONArray array = json.names();
					for (int j = 0; j < array.length(); j++) {
						String key = (String) array.get(j);
						String value = (String) json.get(key);
						Criterion c = getCriterion(key, value);
					    criteria.add(c);	
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		LOGGER.debug("Number of criteria: "+criteria.size());
		return criteria.toArray(new Criterion[criteria.size()]);
	}
	
	
	private static Criterion getCriterion(String key, String value) {
		
		LOGGER.debug("Found criteria "+key+ " "+value);
		if (null == key || null == value)
			return null;
		
		Double dvalue = getFrac(value);
		if (null == dvalue) 
			return null; 
		
		return new Criterion(key, dvalue);
	}


}
