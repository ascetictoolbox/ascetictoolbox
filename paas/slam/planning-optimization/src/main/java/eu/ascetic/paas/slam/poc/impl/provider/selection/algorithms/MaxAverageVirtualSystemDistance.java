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

package eu.ascetic.paas.slam.poc.impl.provider.selection.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.SelectionException;
import eu.ascetic.paas.slam.poc.impl.provider.selection.Criterion;
import eu.ascetic.paas.slam.poc.impl.provider.selection.OfferSelectorAbstractImpl;
import eu.ascetic.paas.slam.poc.impl.provider.selection.WeightedSlat;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplate;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplateParser;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticVirtualSystem;
import eu.ascetic.paas.slam.poc.impl.slaparser.MeasurableAgreementTerm;


public class MaxAverageVirtualSystemDistance extends OfferSelectorAbstractImpl {

	
	static void weightVirtualSystems(List<AsceticSlaTemplate> offers, Criterion[] criteria) {
		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				if (0 == vs.getAgreementTerms().size()) 
					continue;
				double distance = 0;
				for (Criterion c : criteria) {
					MeasurableAgreementTerm mat = 
						(MeasurableAgreementTerm) vs.getAgreementTerm(c.getName());
					if (mat != null)
						distance += Math.pow(mat.getWeight(), 2);
				}
				vs.setWeight(Math.sqrt(distance));
			}
		}
	}
	
	
	
	@Override
	public List<WeightedSlat> evaluate(List<SLATemplate> ssoffs,
			SLATemplate ssprop, Criterion[] criteria)
			throws SelectionException {

		AsceticSlaTemplate proposal = 
			AsceticSlaTemplateParser.getAsceticSlat(ssprop, criteria);

		Map<AsceticSlaTemplate, SLATemplate> offersMap = new HashMap<AsceticSlaTemplate, SLATemplate>();
		for (SLATemplate ssoff : ssoffs) {
			AsceticSlaTemplate offer = AsceticSlaTemplateParser.getAsceticSlat(ssoff, criteria);
			offersMap.put(offer, ssoff);
		}

		List<AsceticSlaTemplate> offers = 
				new ArrayList<AsceticSlaTemplate>(offersMap.keySet());
		
		normalize(offers, proposal);
		print("Normalized offers", offers);
		
		weight(offers, criteria);
		weightVirtualSystems(offers, criteria);
		print("Weighted offers", offers);
		
		List<WeightedSlat> weightedSlats = new ArrayList<WeightedSlat>();
		for (AsceticSlaTemplate offer : offers) {
			Double weight = getAverageWeight(offer.getVirtualSystems());
			WeightedSlat wslat = new WeightedSlat(offersMap.get(offer), weight);
			weightedSlats.add(wslat);
			System.out.println("Weight: "+weight);
		}
		
		return weightedSlats;
	}

}
