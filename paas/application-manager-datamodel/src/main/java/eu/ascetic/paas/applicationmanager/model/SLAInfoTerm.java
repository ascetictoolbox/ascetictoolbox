package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
 * @author: David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * POJO that represents an SLA Info Term for an Application or VM
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sla_info_term", namespace = APPLICATION_MANAGER_NAMESPACE)
public class SLAInfoTerm {
	@XmlElement(name = "sla_term", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String slaTerm;
	@XmlElement(name = "metric_unit", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String metriUnit;
	@XmlElement(name = "comparator", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String comparator;
	@XmlElement(name = "value", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String value;
	@XmlElement(name = "sla_type", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String slaType;
	public String getSlaTerm() {
		return slaTerm;
	}
	public void setSlaTerm(String slaTerm) {
		this.slaTerm = slaTerm;
	}
	public String getMetricUnit() {
		return metriUnit;
	}
	public void setMetricUnit(String metric_unit) {
		this.metriUnit = metric_unit;
	}
	public String getComparator() {
		return comparator;
	}
	public void setComparator(String comparator) {
		this.comparator = comparator;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getSlaType() {
		return slaType;
	}
	public void setSlaType(String slaType) {
		this.slaType = slaType;
	}
}
