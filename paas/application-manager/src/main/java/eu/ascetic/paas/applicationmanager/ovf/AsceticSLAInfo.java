package eu.ascetic.paas.applicationmanager.ovf;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * Represents the SLA info information inside an OVF with ASCETiC features
 */
public class AsceticSLAInfo {
	private String term;
	private String metricUnit;
	private String comparator;
	private String boundaryValue;
	private String type;
	
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getMetricUnit() {
		return metricUnit;
	}
	public void setMetricUnit(String metricUnit) {
		this.metricUnit = metricUnit;
	}
	public String getComparator() {
		return comparator;
	}
	public void setComparator(String comparator) {
		this.comparator = comparator;
	}
	public String getBoundaryValue() {
		return boundaryValue;
	}
	public void setBoundaryValue(String boundaryValue) {
		this.boundaryValue = boundaryValue;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
