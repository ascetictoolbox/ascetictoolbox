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


package eu.ascetic.paas.slam.pac.impl.provider.translation;


public class AsceticAgreementTerm {

	protected String id;
	protected String name;
	protected String unit;
	protected Object value;
	protected operatorType operator;

	public enum operatorType {LESS, LESS_EQUAL, EQUALS, GREATER_EQUAL, GREATER};
	

	
	public AsceticAgreementTerm() {
	}

	
	public AsceticAgreementTerm(String id, String name, String unit, 
			Object value, String operator) {
		this.id = id;
		this.name = name;
		this.unit = unit;
		this.value = value;
		this.operator = getOperator(operator);
	}
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getUnit() {
		return unit;
	}


	public void setUnit(String unit) {
		this.unit = unit;
	}


	public operatorType getOperator() {
		return operator;
	}


	public void setOperator(operatorType operator) {
		this.operator = operator;
	}
	

	
	protected operatorType getOperator(String op) {
		if (op.equals(">"))
			return operatorType.GREATER;
		if (op.equals("<"))
			return operatorType.LESS;
		if (op.equals("=")) 
			return operatorType.EQUALS;
		if (op.equals(">=")) 
			return operatorType.GREATER_EQUAL;
		if (op.equals("<=")) 
			return operatorType.LESS_EQUAL;
		return null;
	}



}
