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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.SelectionException;
import eu.ascetic.paas.slam.poc.impl.config.ConfigManager;
import eu.ascetic.paas.slam.poc.impl.provider.selection.Criterion;
import eu.ascetic.paas.slam.poc.impl.provider.selection.OfferSelectorAbstractImpl;
import eu.ascetic.paas.slam.poc.impl.provider.selection.WeightedSlat;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplate;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplateParser;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticVirtualSystem;
import eu.ascetic.paas.slam.poc.impl.slaparser.MeasurableAgreementTerm;


public class PriceSelection extends OfferSelectorAbstractImpl {

	private static int maxOffersReturned = 4; 
	private static String price_term = "price";
	private static String price_normalization = "ABS";
	private static String[] supportedTerms = {"cpu_speed", "memory", "vm_cores", "price"};
	protected static String algClass = null;


	public List<WeightedSlat> evaluate(List<SLATemplate> ssoffs,SLATemplate ssprop, Criterion[] criteria) throws SelectionException {

		Map<AsceticSlaTemplate, SLATemplate> offersMap = new HashMap<AsceticSlaTemplate, SLATemplate>();
		for (SLATemplate ssoff : ssoffs) {
			AsceticSlaTemplate offer = AsceticSlaTemplateParser.getAsceticSlat(ssoff, criteria);
			offersMap.put(offer, ssoff);
		}

		List<AsceticSlaTemplate> offers = 
				new ArrayList<AsceticSlaTemplate>(offersMap.keySet());


		List<WeightedSlat> weightedSlats = new ArrayList<WeightedSlat>();

		weight(offers);

		for (AsceticSlaTemplate offer : offers) {

			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {

				Iterator<String> iterator = vs.getAgreementTerms().keySet().iterator();

				while (iterator.hasNext()) {
					String termOffer = iterator.next().toString();
					if (price_term.equals(termOffer)) {
						MeasurableAgreementTerm mat = (MeasurableAgreementTerm) vs.getAgreementTerm(price_term);
						if (null != mat) {
//							System.out.println(mat.getWeight());
							vs.setWeight(new Double(mat.getWeight().toString()));
						}
						continue;

					}
				}
				System.out.println("Weight "+vs.getWeight());
			}
			
		}


		for (AsceticSlaTemplate offer : offers) {
			Double weight = getTotalWeight(offer.getVirtualSystems());
			WeightedSlat wslat = new WeightedSlat(offersMap.get(offer), weight);
			weightedSlats.add(wslat);
		}
		return weightedSlats;
	}



	public PriceSelection() {

		ConfigManager cm = ConfigManager.getInstance();
		price_term = cm.getPriceTermName();
		maxOffersReturned = cm.getMaxOffers();
		supportedTerms = cm.getSupportedTerms();
		price_normalization = cm.getPriceNormalization();
		algClass = cm.getAlgorithmClass();

		String printTerms = "Configured agreement terms: ";
		for (String t : supportedTerms) {
			printTerms += "," + t;
		}
		LOGGER.debug("Configured agreement terms: " + printTerms);
		LOGGER.debug("Price normalization method: " + price_normalization);
		LOGGER.debug("Max offers to be returned: " + maxOffersReturned);
	}



	@Override
	public SLATemplate[] selectOptimaSlaTemplates(
			List<SLATemplate> slatList, SLATemplate proposal, Criterion[] criteria) {
		
		List<WeightedSlat> weightedSlats = null;
		try {
			// Call the specific selection algorithm
			weightedSlats = evaluate(slatList, proposal, criteria);

		} catch (SelectionException e) {
			e.printStackTrace();
		}

		Collections.sort(weightedSlats, new WeightedSlat.WeightComparator());

		filter(weightedSlats, maxOffersReturned);

		int i = 0;
		SLATemplate[] finalSlats = new SLATemplate[weightedSlats.size()];
		System.out.println("\n\n============ FINAL SORTED OFFERS ============");
		for (WeightedSlat ws : weightedSlats) {
			ws.getSlat().setPropertyValue(new STND("SelectionWeight"), Double.toString(ws.getWeight()));
			System.out.println("\nSLA Template:" + ws.getSlat() + "\nWeight=" + ws.getWeight());
			finalSlats[i++] = ws.getSlat();
		}

		return finalSlats;
	}



	protected static List<WeightedSlat> filter(List<WeightedSlat> slats, int maxNum) {
		int totrem = slats.size() - maxNum;
		for (int i = 0; i < totrem; i++) {
			slats.remove(slats.size()-1); // pop last elements
		}
		return slats;
	}







	protected static void weight(List<AsceticSlaTemplate> offers) 
			throws SelectionException {
		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
					MeasurableAgreementTerm mat = (MeasurableAgreementTerm) vs.getAgreementTerm(price_term);
					if (null != mat) {
						mat.setWeight(mat.getValue()); 
					}
				
			}
		}
	}



	protected Double getMaximumWeight(List<AsceticVirtualSystem> csvs) {
		double max = 0;
		for (AsceticVirtualSystem c : csvs) {
			if (null != c.getWeight() && c.getWeight() > max)
				max = c.getWeight();
		}
		return max;
	}



	protected Double getAverageWeight(List<AsceticVirtualSystem> csvs) {
		double sum = 0;
		int matterSize = 0;
		for (AsceticVirtualSystem c : csvs) {
			if (null != c.getWeight()) {
				sum += c.getWeight();
				matterSize++;
			}
		}
		return (sum / matterSize) ;
	}
	
	
	
	protected Double getTotalWeight(List<AsceticVirtualSystem> csvs) {
		double tot = 0;
		for (AsceticVirtualSystem c : csvs) {
			if (null != c.getWeight())
				tot += c.getWeight();
		}
		return tot;
	}



	// for debugging purposes
	protected static void print(String message, List<AsceticSlaTemplate> offers) {
		System.out.println(message);
		LOGGER.debug(message);
		int i = 0;
		for (AsceticSlaTemplate offer : offers) {
			LOGGER.debug("OFFER [" + i++ + "] :\n" + offer);
		}
	}


	private static final Logger LOGGER = Logger.getLogger(PriceSelection.class);


}
