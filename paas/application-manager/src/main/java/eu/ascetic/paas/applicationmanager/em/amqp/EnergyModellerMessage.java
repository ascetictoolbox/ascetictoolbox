package eu.ascetic.paas.applicationmanager.em.amqp;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import eu.ascetic.paas.applicationmanager.util.EqualsUtil;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * POJO object that represents the Message sended by the Energy Modeller
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnergyModellerMessage {
	private String provider;
	private String applicationid;
	private String eventid;
	private String deploymentid;
	private List<String> vms;
	private String unit;
	private String generattiontimestamp;
	private String referredtimestamp;
	private String value;
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getApplicationid() {
		return applicationid;
	}
	public void setApplicationid(String applicationid) {
		this.applicationid = applicationid;
	}
	
	public String getEventid() {
		return eventid;
	}
	public void setEventid(String eventid) {
		this.eventid = eventid;
	}
	
	public List<String> getVms() {
		return vms;
	}
	public void setVms(List<String> vms) {
		this.vms = vms;
	}
	
	public String getDeploymentid() {
		return deploymentid;
	}
	
	public void setDeploymentid(String deploymentid) {
		this.deploymentid = deploymentid;
	}
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public String getGenerattiontimestamp() {
		return generattiontimestamp;
	}
	public void setGenerattiontimestamp(String generattiontimestamp) {
		this.generattiontimestamp = generattiontimestamp;
	}
	
	public String getReferredtimestamp() {
		return referredtimestamp;
	}
	public void setReferredtimestamp(String referredtimestamp) {
		this.referredtimestamp = referredtimestamp;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object aThat) {
		if(this == aThat) return true;
		
		if(!(aThat instanceof EnergyModellerMessage)) return false;
		
		EnergyModellerMessage that = (EnergyModellerMessage) aThat;
		
		if(EqualsUtil.areEqual(this.provider, that.getProvider()) && 
		   EqualsUtil.areEqual(this.applicationid, that.applicationid) &&
		   EqualsUtil.areEqual(this.deploymentid, that.deploymentid) &&
		   EqualsUtil.areEqual(this.eventid, that.eventid) &&
		   EqualsUtil.areEqual(this.generattiontimestamp, that.generattiontimestamp) &&
		   EqualsUtil.areEqual(this.referredtimestamp, that.referredtimestamp) &&
		   EqualsUtil.areEqual(this.value, that.value) && 
		   EqualsUtil.areEqual(this.unit, that.unit) &&
		   EqualsUtil.areEqual(this.vms, that.vms)) {
			return true;
		} else {
			return false;
		}
	}
}
