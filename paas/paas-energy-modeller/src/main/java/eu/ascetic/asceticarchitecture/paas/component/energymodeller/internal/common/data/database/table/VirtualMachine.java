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

/**
 * 
 * @author sommacam
 * represent data stored inside the database about a vm
 */
public class VirtualMachine {
	
	// M. Fontanella - 10 Feb 2016 - begin
	// M. Fontanella - 20 Jan 2016 - begin
	private String providerid;
	// M. Fontanella - 20 Jan 2016 - end
	private String applicationid;
	private String deploymentid;
	private String vmid;
	// M. Fontanella - 10 Feb 2016 - end
	private long start;
	private long stop;
	// M. Fontanella - 10 Feb 2016 - begin
	private int profileid;
	private int modelid;
	private String iaasid;
		
	// M. Fontanella - 20 Jan 2016 - begin
	public String getProviderid() {
		return providerid;
	}
	public void setProviderid(String providerid) {
		this.providerid = providerid;
	}
	// M. Fontanella - 20 Jan 2016 - end
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
	public String getVmid() {
		return vmid;
	}
	public void setVmid(String vmid) {
		this.vmid = vmid;
	}
	// M. Fontanella - 10 Feb 2016 - end
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
	// M. Fontanella - 10 Feb 2016 - end
	public int getProfileid() {
		return profileid;
	}
	public void setProfileid(int profileid) {
		this.profileid = profileid;
	}
	public int getModelid() {
		return modelid;
	}
	public void setModelid(int modelid) {
		this.modelid = modelid;
	}
	public String getIaasid() {
		return iaasid;
	}
	public void setIaasid(String iaasid) {
		this.iaasid = iaasid;
	}
	// M. Fontanella - 10 Feb 2016 - end
	
	

}
