package eu.ascetic.paas.applicationmanager.event;

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
 * POJO object that represents the event information for a deployment internally
 */
public class DeploymentEvent {
	private String applicationName;
	private int deploymentId;
	private String deploymentStatus;
	private boolean automaticNegotiation;
	private int providerId=-1;
	
	public int getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(int deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	public String getDeploymentStatus() {
		return deploymentStatus;
	}
	public void setDeploymentStatus(String deploymentStatus) {
		this.deploymentStatus = deploymentStatus;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public boolean isAutomaticNegotiation() {
		return automaticNegotiation;
	}
	public void setAutomaticNegotiation(boolean automaticNegotiation) {
		this.automaticNegotiation = automaticNegotiation;
	}
	
	public int getProviderId() {
		return providerId;
	}
	public void setProviderId(int providerId) {
		this.providerId = providerId;
	}
}
