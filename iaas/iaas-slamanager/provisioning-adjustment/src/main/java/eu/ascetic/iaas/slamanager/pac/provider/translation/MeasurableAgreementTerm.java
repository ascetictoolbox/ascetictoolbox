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


package eu.ascetic.iaas.slamanager.pac.provider.translation;

import java.text.DecimalFormat;


public class MeasurableAgreementTerm extends AsceticAgreementTerm {

	private final static double Ki = 1024;
	
	private Double value;
	
	private Double weight;
	
	public MeasurableAgreementTerm(String name, String unit, 
			String value, String operator) {
		
		this.name = name;
		this.unit = unit.startsWith("xsd") ? unit.substring(4) : unit;
		try {
			this.value = new Double(value);
		} catch (NumberFormatException e) {
			System.out.println("Conversion exception: "+ e.getMessage());
			this.value = (double) 0;
		}
		if (operator.equals(">")) {
			this.operator = operatorType.GREATER;
		} else if (operator.equals("<")) {
			this.operator = operatorType.LESS;
		} else if (operator.equals("=")) {
			this.operator = operatorType.EQUALS;
		} else if (operator.equals(">=")) {
			this.operator = operatorType.GREATER_EQUAL;
		} else if (operator.equals("<=")) {
			this.operator = operatorType.LESS_EQUAL;
		}
		normalize(this);
	}

	
	public MeasurableAgreementTerm(String id, String name, String unit, 
			String value, String operator) {
		this(name, unit, value, operator);
		this.id = id;
	}

	
	public void setValue(Double value) {
		this.value = value;
	}
	
	
	public Double getValue() {
		return value;
	}

	
	public Double getWeight() {
		return weight;
	}


	public void setWeight(Double weight) {
		this.weight = weight;
	}

	
	public static void normalize(MeasurableAgreementTerm at) {
		if ("KB".equals(at.unit)) {
			at.unit = "GB";
			at.value /= (Ki*Ki);
		} else if ("MB".equals(at.unit)) {
			at.unit = "GB";
			at.value /= (Ki);
		} else if ("TB".equals(at.unit)) {
			at.unit = "GB";
			at.value *= (Ki);
		} else if ("MHz".equals(at.unit)) {
			at.unit = "GHz";
			at.value /= 1000;
		} else if ("KHz".equals(at.unit)) {
			at.unit = "GHz";
			at.value /= 1000000;
		} else if ("THz".equals(at.unit)) {
			at.unit = "GHz";
			at.value *= 1000;
		}
	}
	

    private DecimalFormat pf = new DecimalFormat("#.####");
    
	private Double roundDecimals(Double d) { 
		if (d == null) 
			return null;
 	    return Double.valueOf(pf.format(d));
	}  
	
	
	public String toString() {
//		return "Name: " + name + "\toperator: " + operator +
//				"\tvalue: " + roundDecimals(value) + "\tunit: " + 
//				unit + "\tweight: " + roundDecimals(weight);
		return "Name: " + name + "\toperator: " + operator +
				"\tvalue: " + value + "\tunit: " + 
				unit + "\tweight: " + weight;
	}

}
