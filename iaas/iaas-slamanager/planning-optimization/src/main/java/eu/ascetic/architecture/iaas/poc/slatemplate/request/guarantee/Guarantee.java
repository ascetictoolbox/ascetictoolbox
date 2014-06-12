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

package eu.ascetic.architecture.iaas.poc.slatemplate.request.guarantee;

import eu.ascetic.architecture.iaas.poc.enums.AsceticAgreementTerm;

public abstract class Guarantee {

	protected AsceticAgreementTerm agreementTerm;

	protected String id;

	protected String domain;

	public Guarantee(String id, AsceticAgreementTerm t) {
		this.id = id;
		this.agreementTerm = t;
	}

	public AsceticAgreementTerm getAgreementTerm() {
		return agreementTerm;
	}

	public void setAgreementTerm(AsceticAgreementTerm agreementTerm) {
		this.agreementTerm = agreementTerm;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String toString() {
		String result = "Guarantee: " + id + " , domain: " + domain + " , agreementTerm: " + agreementTerm.toString() + " ";
		return result;
	}

}
