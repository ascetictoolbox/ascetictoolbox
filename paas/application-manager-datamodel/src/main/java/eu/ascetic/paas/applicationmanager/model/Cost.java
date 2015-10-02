package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * Basic XML representation for any Cost
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cost", namespace = APPLICATION_MANAGER_NAMESPACE)
public class Cost {
	@XmlAttribute
	private String href;
	@XmlElement(name = "energy-value", namespace = APPLICATION_MANAGER_NAMESPACE)
	private Double energyValue;
	@XmlElement(name = "energy-description", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String energyDescription;
	@XmlElement(name = "power-value", namespace = APPLICATION_MANAGER_NAMESPACE)
	private Double powerValue;
	@XmlElement(name = "power-description", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String powerDescription;
	@XmlElement(name = "charges", namespace = APPLICATION_MANAGER_NAMESPACE)
	private Double charges;
	@XmlElement(name = "charges-description", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String chargesDescription;
	@XmlElement(name="link", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<Link> links;
	
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	
	public Double getEnergyValue() {
		return energyValue;
	}
	public void setEnergyValue(Double energyValue) {
		this.energyValue = energyValue;
	}
	
	public String getEnergyDescription() {
		return energyDescription;
	}
	public void setEnergyDescription(String energyDescription) {
		this.energyDescription = energyDescription;
	}
	
	public Double getPowerValue() {
		return powerValue;
	}
	public void setPowerValue(Double powerValue) {
		this.powerValue = powerValue;
	}
	
	public String getPowerDescription() {
		return powerDescription;
	}
	public void setPowerDescription(String powerDescription) {
		this.powerDescription = powerDescription;
	}
	
	public Double getCharges() {
		return charges;
	}
	public void setCharges(Double charges) {
		this.charges = charges;
	
	}
	public String getChargesDescription() {
		return chargesDescription;
	}
	public void setChargesDescription(String chargesDescription) {
		this.chargesDescription = chargesDescription;
	}
	
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
}
