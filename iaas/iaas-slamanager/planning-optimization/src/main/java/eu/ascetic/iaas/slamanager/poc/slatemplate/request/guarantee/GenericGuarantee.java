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

package eu.ascetic.iaas.slamanager.poc.slatemplate.request.guarantee;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.iaas.slamanager.poc.enums.AsceticAgreementTerm;
import eu.ascetic.iaas.slamanager.poc.enums.OperatorType;

public class GenericGuarantee extends Guarantee {

	private List<Value> values = null;

	public GenericGuarantee(String gn, AsceticAgreementTerm t) {
		super(gn, t);
		values = new ArrayList<GenericGuarantee.Value>();
	}

	public void addConstraint(Value c) {
		values.add(c);
	}

	public void removeConstraint(Value c) {
		values.remove(c);
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += "[";
		for (Value c : values) {
			s += c.toString();
		}
		s += "]\n";
		return s;
	}

	public class Value {

		private String type = null;

		private String value = null;

		protected OperatorType operator = null;

		public Value(String type, String value, OperatorType operator) {
			this.type = type;
			this.value = value;
			this.operator = operator;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public OperatorType getOperator() {
			return operator;
		}

		public void setOperator(OperatorType operator) {
			this.operator = operator;
		}

		@Override
		public String toString() {
			return "type: " + type + " value: " + value + " operator: " + operator.toString() + " ";
		}
	}

}
