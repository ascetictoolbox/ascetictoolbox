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

import java.util.HashMap;

import eu.ascetic.architecture.iaas.poc.enums.AsceticAgreementTerm;

public class ActionGuarantee extends GenericGuarantee {

	private String actionID = null;

	private String actionType = null;

	private String conditionId = null;

	private String endpoint = null;

	private String operation = null;

	private HashMap<String, String> parameter = null;

	public ActionGuarantee(String gn, AsceticAgreementTerm t, String actionId) {
		super(gn, t);
		this.actionID = actionId;
		parameter = new HashMap<String, String>();
	}

	public String getActionID() {
		return actionID;
	}

	public void setActionID(String actionID) {
		this.actionID = actionID;
	}

	public String getConditionId() {
		return conditionId;
	}

	public void setConditionId(String conditionId) {
		this.conditionId = conditionId;
	}

	public void addParameter(String key, String value) {
		parameter.put(key, value);
	}

	public void removeParameter(String key) {
		parameter.remove(key);
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

}
