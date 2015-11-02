package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * Basic XML representation for any Energy Sample comming from the ASCETiC Energy Modeller
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "energy-sample", namespace = APPLICATION_MANAGER_NAMESPACE)
public class EnergySample {
	@XmlElement(name = "vmid", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String vmid;
	@XmlElement(name = "timestampBeging", namespace = APPLICATION_MANAGER_NAMESPACE)
	private long timestampBeging;
	@XmlElement(name = "timestampEnd", namespace = APPLICATION_MANAGER_NAMESPACE)
	private long timestampEnd;
	@XmlElement(name = "evalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double evalue;
	@XmlElement(name = "pvalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double pvalue;
	@XmlElement(name = "cvalue", namespace = APPLICATION_MANAGER_NAMESPACE)
	private double cvalue;

	public double getEvalue() {
		return evalue;
	}
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	public double getPvalue() {
		return pvalue;
	}
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	public double getCvalue() {
		return cvalue;
	}
	public void setCvalue(double cvalue) {
		this.cvalue = cvalue;
	}
	public long getTimestampBeging() {
		return timestampBeging;
	}
	public void setTimestampBeging(long timestampBeging) {
		this.timestampBeging = timestampBeging;
	}
	public long getTimestampEnd() {
		return timestampEnd;
	}
	public void setTimestampEnd(long timestampEnd) {
		this.timestampEnd = timestampEnd;
	}
	public String getVmid() {
		return vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
}
