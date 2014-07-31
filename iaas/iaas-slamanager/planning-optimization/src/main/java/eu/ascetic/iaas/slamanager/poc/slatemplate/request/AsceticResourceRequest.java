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

package eu.ascetic.iaas.slamanager.poc.slatemplate.request;

import java.util.ArrayList;

import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.GenericGuarantee;
import eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee.Guarantee;

public abstract class AsceticResourceRequest extends AsceticRequest {

	public AsceticResourceRequest(String id) {
		super(id);
	}

	public ArrayList<GenericGuarantee> getGenericGuarantees() {
		ArrayList<GenericGuarantee> genericGuarantees = new ArrayList<GenericGuarantee>();
		for (Guarantee g : guarantees) {
			if (g instanceof GenericGuarantee) {
				genericGuarantees.add((GenericGuarantee) g);
			}
		}
		return genericGuarantees;
	}

	@Override
	public String toString() {
		String result = "AsceticResource: " + id + " , Guarantees:\n";
		for (Guarantee g : guarantees) {
			result += g.toString();
		}
		return result;
	}

}
