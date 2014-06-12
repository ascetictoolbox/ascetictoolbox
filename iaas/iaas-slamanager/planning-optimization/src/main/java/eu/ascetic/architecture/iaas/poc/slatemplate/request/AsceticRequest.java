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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.slasoi.slamodel.primitives.Expr;

import eu.ascetic.architecture.iaas.poc.slatemplate.request.guarantee.Guarantee;

public abstract class AsceticRequest {

	protected String id = null;

	protected String ovfId = null;

	protected Collection<Guarantee> guarantees = null;

	protected HashMap<String, Expr> variables = null;

	public AsceticRequest(String id) {
		this.id = id;
		guarantees = new HashSet<Guarantee>();
		variables = new HashMap<String, Expr>();
	}

	public void addGuarantee(Guarantee g) {
		guarantees.add(g);
	}

	public void removeGuarantee(Guarantee g) {
		guarantees.remove(g);
	}

	public void addVariable(String key, Expr value) {
		variables.put(key, value);
	}

	public void removeGuarantee(String key) {
		variables.remove(key);
	}

	public void setVariables(HashMap<String, Expr> v) {
		this.variables = v;
	}

	public String getId() {
		return this.id;
	}

	public String getOvfId() {
		return ovfId;
	}

	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}

	public Collection<Guarantee> getGuarantees() {
		return guarantees;
	}

	public HashMap<String, Expr> getVariables() {
		return variables;
	}

}
