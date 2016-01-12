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
	
	private String app_id;
	private int deploy_id;
	private int vm_id;
	private long start;
	private long stop;
	private int profile_id;
	private int model_id;
	private String iaas_id;
	
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public int getDeploy_id() {
		return deploy_id;
	}
	public void setDeploy_id(int deploy_id) {
		this.deploy_id = deploy_id;
	}
	public int getVm_id() {
		return vm_id;
	}
	public void setVm_id(int vm_id) {
		this.vm_id = vm_id;
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
	public int getProfile_id() {
		return profile_id;
	}
	public void setProfile_id(int profile_id) {
		this.profile_id = profile_id;
	}
	public int getModel_id() {
		return model_id;
	}
	public void setModel_id(int model_id) {
		this.model_id = model_id;
	}
	public String getIaas_id() {
		return iaas_id;
	}
	public void setIaas_id(String iaas_id) {
		this.iaas_id = iaas_id;
	}
	
	

}