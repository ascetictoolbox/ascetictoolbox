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


package eu.ascetic.paas.slam.poc.impl.slaparser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class AsceticVirtualSystem {

	private String id;
	
	private HashMap<String, AsceticAgreementTerm> agreementTerms;
	
	private Double weight;

	public AsceticVirtualSystem() {
		agreementTerms = new HashMap<String, AsceticAgreementTerm>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, AsceticAgreementTerm> getAgreementTerms() {
		return agreementTerms;
	}

	public AsceticAgreementTerm getAgreementTerm(String termName) {
		return agreementTerms.get(termName); 
	}
	
	public void addAgreementTerm(AsceticAgreementTerm at) {
		String termName = at.name;
		if (agreementTerms.containsKey(termName)) {
			if (at.operator.ordinal() > getAgreementTerm(termName).operator.ordinal()) {
				agreementTerms.put(at.name,  at);
			}
		} else {
			agreementTerms.put(at.name,  at);
		}
	}

	public void removeAgreementTerm(AsceticAgreementTerm at) {
		agreementTerms.remove(at);
	}

	public void setAgreementTerms(Map<String, AsceticAgreementTerm> agreementTerms) {
		this.agreementTerms = (HashMap<String, AsceticAgreementTerm>) agreementTerms;
	}

	
	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}


	
	public String toString() {
		String s = "\nID: " + id + "\nAgreement terms: ";
	    Iterator<String> iterator = agreementTerms.keySet().iterator();  
	       
	    while (iterator.hasNext()) {  
	       String key = iterator.next().toString(); 
	       String value = agreementTerms.get(key).toString();  
	       s += ("\n" + value);
	    }  
	    s += ("\nVS weight: " + weight);
		return s;

	}
}
