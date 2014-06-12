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

package eu.ascetic.architecture.iaas.poc.slatemplate.request;

import java.util.ArrayList;

import eu.ascetic.architecture.iaas.poc.enums.AsceticAgreementTerm;
import eu.ascetic.architecture.iaas.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.architecture.iaas.poc.slatemplate.request.guarantee.Guarantee;
import eu.ascetic.architecture.iaas.poc.slatemplate.request.guarantee.ResourceGuarantee;

public class VirtualSystem extends AsceticResourceRequest {

	public VirtualSystem(String id) {
		super(id);
	}

	public ArrayList<ResourceGuarantee> getResourcesOfTerm(AsceticAgreementTerm t) {
		ArrayList<ResourceGuarantee> resourceRequests = new ArrayList<ResourceGuarantee>();
		for (Guarantee g : guarantees) {
			if (g instanceof ResourceGuarantee && g.getAgreementTerm().equals(t)) {
				resourceRequests.add((ResourceGuarantee) g);
			}
		}
		return resourceRequests;
	}

	public ArrayList<ResourceGuarantee> getResourceGuarantees() {
		ArrayList<ResourceGuarantee> resourceGuarantees = new ArrayList<ResourceGuarantee>();
		for (Guarantee g : guarantees) {
			if (g instanceof ResourceGuarantee) {
				resourceGuarantees.add((ResourceGuarantee) g);
			}
		}
		return resourceGuarantees;
	}

	public GenericGuarantee getReservationGuarantee() {
		for (Guarantee g : guarantees) {
			if (g instanceof GenericGuarantee && g.getAgreementTerm().equals(AsceticAgreementTerm.reserve))
				return (GenericGuarantee) g;
		}
		return null;
	}
}
