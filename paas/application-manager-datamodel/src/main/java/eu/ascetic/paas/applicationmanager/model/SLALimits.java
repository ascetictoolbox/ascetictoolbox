package eu.ascetic.paas.applicationmanager.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import java.util.ArrayList;
import java.util.List;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * POJO Object that represents the SLA Limits for a Deployment
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sla_limits", namespace = APPLICATION_MANAGER_NAMESPACE)
public class SLALimits {
	@XmlElement(name ="power", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String power;
	@XmlElement(name ="power_unit", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String powerUnit;
	@XmlElement(name ="energy", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String energy;
	@XmlElement(name ="energy_units", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String energyUnit;
	@XmlElement(name ="cost", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String cost;
	@XmlElement(name ="cost_units", namespace = APPLICATION_MANAGER_NAMESPACE)
	private String costUnit;
	@XmlElement(name ="vm", namespace = APPLICATION_MANAGER_NAMESPACE)
	private List<SLAVMLimits> vmLimits = new ArrayList<SLAVMLimits>();
	
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
	public String getPowerUnit() {
		return powerUnit;
	}
	public void setPowerUnit(String powerUnit) {
		this.powerUnit = powerUnit;
	}
	public String getEnergy() {
		return energy;
	}
	public void setEnergy(String energy) {
		this.energy = energy;
	}
	public String getEnergyUnit() {
		return energyUnit;
	}
	public void setEnergyUnit(String energyUnit) {
		this.energyUnit = energyUnit;
	}
	public String getCost() {
		return cost;
	}
	public void setCost(String cost) {
		this.cost = cost;
	}
	public String getCostUnit() {
		return costUnit;
	}
	public void setCostUnit(String costUnit) {
		this.costUnit = costUnit;
	}

	public List<SLAVMLimits> getVmLimits() {
		return vmLimits;
	}

	public void setVmLimits(List<SLAVMLimits> vmLimits) {
		this.vmLimits = vmLimits;
	}
}
