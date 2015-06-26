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

package eu.ascetic.paas.slam.poc.impl.provider.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.slasoi.slamodel.primitives.STND;
import org.slasoi.slamodel.sla.SLATemplate;

import eu.ascetic.paas.slam.poc.exceptions.SelectionException;
import eu.ascetic.paas.slam.poc.impl.config.ConfigManager;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticAgreementTerm;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticSlaTemplate;
import eu.ascetic.paas.slam.poc.impl.slaparser.AsceticVirtualSystem;
import eu.ascetic.paas.slam.poc.impl.slaparser.MeasurableAgreementTerm;


public abstract class OfferSelectorAbstractImpl implements OfferSelector {

	private static int maxOffersReturned = 4; 
	private static String price_term = "price";
	private static String price_normalization = "ABS";
	private static String[] supportedTerms = {"cpu_speed", "memory", "vm_cores", "price"};
    protected static String algClass = null;
	
	// The evaluation function must be implemented by each selection algorithm
	public abstract List<WeightedSlat> evaluate(List<SLATemplate> offers, 
			SLATemplate proposal, Criterion[] criteria) throws SelectionException;
	
	
	
	public OfferSelectorAbstractImpl() {

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
			List<SLATemplate> slatList, SLATemplate proposal, Criterion[] userCriteria) {

		Criterion[] criteria = getAbsoluteCriteria(userCriteria);
		
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
		LOGGER.debug("\n\n============ FINAL SORTED OFFERS ============");
		for (WeightedSlat ws : weightedSlats) {
			ws.getSlat().setPropertyValue(new STND("SelectionWeight"), Double.toString(ws.getWeight()));
			LOGGER.debug("\nSLA Template:" + ws.getSlat() + "\nWeight=" + ws.getWeight());
//			System.out.println("\nSLA Template:" + ws.getSlat() + "\nWeight=" + ws.getWeight());
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
	

	
	protected static Double normalizedValue(MeasurableAgreementTerm offer,
			MeasurableAgreementTerm proposal) {
		if (0 == offer.getValue() || 0 == proposal.getValue())
			return (double) 0;

		if ( proposal.getOperator() == AsceticAgreementTerm.operatorType.GREATER
			|| proposal.getOperator() == AsceticAgreementTerm.operatorType.GREATER_EQUAL
			|| proposal.getOperator() == AsceticAgreementTerm.operatorType.EQUALS) {
			return (offer.getValue() / proposal.getValue());
		} else {
			return (proposal.getValue() / offer.getValue());
		}
	}

	
	
	protected static void weight(List<AsceticSlaTemplate> offers, Criterion[] criteria) 
			throws SelectionException {
		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				for (Criterion c : criteria) {
					MeasurableAgreementTerm mat = (MeasurableAgreementTerm) vs.getAgreementTerm(c.getName());
					if (null != mat) {
						mat.setWeight(mat.getWeight() * c.getWeight()); 
					}
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
	
	
	
	protected static void normalize(List<AsceticSlaTemplate> offers,
			AsceticSlaTemplate proposal) throws SelectionException {

		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				AsceticVirtualSystem propVs = proposal.getVirtualSystem(vs.getId());

				Iterator<String> iterator = vs.getAgreementTerms().keySet().iterator();

				while (iterator.hasNext()) {
					String termOffer = iterator.next().toString();
					if (price_term.equals(termOffer))
						continue;
					MeasurableAgreementTerm propAt = (MeasurableAgreementTerm) propVs
							.getAgreementTerm(termOffer);
					if (null == propAt) {
						throw new SelectionException(
								"Normalization failure: could not find corresponding term in proposal for term " 
										+ termOffer);
					}
					MeasurableAgreementTerm offerAt = (MeasurableAgreementTerm) vs
							.getAgreementTerms().get(termOffer);

					offerAt.setWeight(normalizedValue(offerAt, propAt));
				}
			}
		}
		
		if (price_normalization.equals("REL")) { 
			normalizePricesRel(offers); } else {
		    normalizePricesAbs(offers);
		}
	}

	
	// The weight is computed as the reciprocal for price
	private static void normalizePricesAbs(List<AsceticSlaTemplate> offers)
			throws SelectionException {
		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				MeasurableAgreementTerm mat = (MeasurableAgreementTerm) vs.getAgreementTerm(price_term);
				if (null != mat) {
					mat.setWeight(1 / mat.getValue());
				}
			}
		}
	}
	
	
	// This normalization process compare prices among them
	private static void normalizePricesRel(List<AsceticSlaTemplate> offers)
			throws SelectionException {

		HashMap<String, Double> vsSumPrices = new HashMap<String, Double>();
		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				AsceticAgreementTerm cat = vs.getAgreementTerm(price_term);
				if (null != cat) {
					Double sum = vsSumPrices.get(vs.getId()) ;
					if (null == sum) sum = (double) 0;
					vsSumPrices.put(vs.getId(), sum + ((MeasurableAgreementTerm) cat).getValue());
				}
			}
		}

		for (AsceticSlaTemplate offer : offers) {
			for (AsceticVirtualSystem vs : offer.getVirtualSystems()) {
				MeasurableAgreementTerm mat = (MeasurableAgreementTerm) vs.getAgreementTerm(price_term);
				if (null != mat) {
					Double sumPrice = vsSumPrices.get(vs.getId());
					mat.setWeight( (sumPrice - mat.getValue()) / ((offers.size()-1) * mat.getValue()) );
				}
			}
		}
	}


	
	// for debugging purposes
	protected static void print(String message, List<AsceticSlaTemplate> offers) {
		LOGGER.debug(message);
		int i = 0;
		for (AsceticSlaTemplate offer : offers) {
			LOGGER.debug("OFFER [" + i++ + "] :\n" + offer);
		}
	}
	
	
	private static Criterion[] getAbsoluteCriteria(Criterion[] criteria) {

		if (criteria == null) {
			Criterion[] crits = new Criterion[supportedTerms.length];
			for (int i = 0; i < crits.length; i++) {
				crits[i] = new Criterion(supportedTerms[i], 1); // to be checked
			}
			return crits;
		}
		
		HashMap<String, Criterion> hash = new HashMap<String, Criterion>();
		
		if (criteria != null) {
			for (Criterion c : criteria) {
				hash.put(c.getName(), c);
			}
		}
		
		for (String term: supportedTerms) {
			if (!hash.containsKey(term)) {
				hash.put(term, new Criterion(term, 0));
			}
		}
		
		return new ArrayList<Criterion>(hash.values()).toArray(new Criterion[hash.size()]);
	}
	

	private static final Logger LOGGER = Logger.getLogger(OfferSelectorAbstractImpl.class);

	
}
