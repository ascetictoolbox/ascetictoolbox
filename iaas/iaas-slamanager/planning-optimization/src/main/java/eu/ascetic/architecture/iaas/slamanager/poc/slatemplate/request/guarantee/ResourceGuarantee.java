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

package eu.ascetic.architecture.iaas.slamanager.poc.slatemplate.request.guarantee;

import eu.ascetic.architecture.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.architecture.iaas.slamanager.poc.utils.AsceticUnits;
import eu.ascetic.architecture.iaas.slamanager.poc.utils.Unit;

public class ResourceGuarantee extends Guarantee {

	private double min;
	private double max;
	private double default_value;

	private Unit unit;

	public ResourceGuarantee(String id, AsceticAgreementTerm t) {
		super(id, t);
		if (t.equals(AsceticAgreementTerm.cpu_speed))
			unit = AsceticUnits.DEFAULT_FREQUENCY_UNIT;
		else if (t.equals(AsceticAgreementTerm.memory))
			unit = AsceticUnits.DEFAULT_MEMORY_UNIT;
		else if (t.equals(AsceticAgreementTerm.vm_cores))
			unit = AsceticUnits.DEFAULT_CORE_UNIT;
		setMin(-1);
		setMax(-1);
		setDefault(-1);
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getDefault() {
		return default_value;
	}

	public void setDefault(double default_value) {
		this.default_value = default_value;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setRanges(double min, double max, double default_Value) {
		setMin(min);
		setMax(max);
		setDefault(default_Value);
	}

	@Override
	public String toString() {
		String result = super.toString();
		result += "min_value: " + min + " , max_value: " + max + " , def_value: " + default_value + " Unit: " + unit.toString() + "\n";
		return result;
	}
}
