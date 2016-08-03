package eu.ascetic.paas.applicationmanager.amqp.model;

import static eu.ascetic.paas.applicationmanager.model.Dictionary.APPLICATION_MANAGER_NAMESPACE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


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
 * POJO Representing the part of VM information sent into a message when an
 * event is fired by the Application Manager for an specific deployment.
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class VM {
	private String vmId;
	private String iaasVmId;
	private String iaasMonitoringVmId;
	private String ovfId;
	private String status;
	private String metricName;
	private String providerId;
	private double value;
	private String units;
	private long timestamp;
	private int cpu;
	private long ram;
	private long swap;
	private long disk;
	private int priceSchema;
	
	public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public long getRam() {
		return ram;
	}
	public void setRam(long ram) {
		this.ram = ram;
	}
	public long getSwap() {
		return swap;
	}
	public void setSwap(long swap) {
		this.swap = swap;
	}
	public long getDisk() {
		return disk;
	}
	public void setDisk(long disk) {
		this.disk = disk;
	}
	public int getPriceSchema() {
		return priceSchema;
	}
	public void setPriceSchema(int priceSchema) {
		this.priceSchema = priceSchema;
	}
	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	
	public String getIaasVmId() {
		return iaasVmId;
	}
	public void setIaasVmId(String iaasVmId) {
		this.iaasVmId = iaasVmId;
	}
	
	public String getIaasMonitoringVmId() {
		return iaasMonitoringVmId;
	}
	public void setIaasMonitoringVmId(String iaasMonitoringVmId) {
		this.iaasMonitoringVmId = iaasMonitoringVmId;
	}
	
	public String getOvfId() {
		return ovfId;
	}
	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMetricName() {
		return metricName;
	}
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
}
