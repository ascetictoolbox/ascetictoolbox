/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.database.table;

import java.sql.Timestamp;

public class EnergyModellerMonitoring {

	private String monitoringid;
	private String providerid;
	private String applicationid;
	private String deploymentid;
	private long start;
	private long stop;
	private String events;
	private boolean status;
	// TODO will store data for future reference as: energy estimation ecc..
	public String getMonitoringid() {
		return monitoringid;
	}
	public void setMonitoringid(String monitoringid) {
		this.monitoringid = monitoringid;
	}
	public String getProviderid() {
		return providerid;
	}
	public void setProviderid(String providerid) {
		this.providerid = providerid;
	}
	public String getApplicationid() {
		return applicationid;
	}
	public void setApplicationid(String applicationid) {
		this.applicationid = applicationid;
	}
	public String getDeploymentid() {
		return deploymentid;
	}
	public void setDeploymentid(String deploymentid) {
		this.deploymentid = deploymentid;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getStop() {
		return stop;
	}
	public void setStop(long stop) {
		this.stop = stop;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public String getEvents() {
		return events;
	}
	public void setEvents(String events) {
		this.events = events;
	}
	
}
