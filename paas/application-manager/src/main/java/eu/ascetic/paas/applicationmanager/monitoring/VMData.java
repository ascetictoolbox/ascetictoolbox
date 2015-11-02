package eu.ascetic.paas.applicationmanager.monitoring;

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
 * Temporal POJO object that contains the data necessary to resend the messages from the the IaaS AMQP
 * to the PaaS AMQP
 *
 */
public class VMData {
	private String applicationId;
	private String deploymentId;
	private String vmId;
	private String vmIaaSId;
	private String ovfId;
	
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	public String getVmId() {
		return vmId;
	}
	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	
	public String getVmIaaSId() {
		return vmIaaSId;
	}
	
	public void setVmIaaSId(String vmIaaSId) {
		this.vmIaaSId = vmIaaSId;
	}
	
	public String getOvfId() {
		return ovfId;
	}
	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
	}
}
