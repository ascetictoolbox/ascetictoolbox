package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.List;

import org.apache.log4j.Logger;

import es.bsc.vmmclient.models.ImageToUpload;
import es.bsc.vmmclient.models.ImageUploaded;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmCost;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.paas.applicationmanager.conf.Configuration;


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
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This version of the VmManager Client makes usage of the library already implemented by
 * David Ortiz in the BSC.
 */
public class VmManagerClientBSSC implements VmManagerClient {
	private static Logger logger = Logger.getLogger(VmManagerClientBSSC.class);
	private String url;
	
	public VmManagerClientBSSC() {
		this.url = Configuration.vmManagerServiceUrl;
		logger.info("Connecting to URL for VMM: " + url);
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public List<ImageUploaded> getAllImages() {
		
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getImages();
	}

	public ImageUploaded getImage(String id) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getImage(id);
	}

	public List<VmDeployed> getAllVMs() {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getVms();
	}

	public VmDeployed getVM(String id) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getVm(id);
	}

	public List<VmDeployed> getVmsOfApp(String appId) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getAppVms(appId);
	}

	public List<String> deployVMs(List<Vm> vms) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.deployVms(vms);
	}

	public void changeStateVm(String vmId, String action) {
		// TODO Auto-generated method stub

	}

	public void deleteVM(String vmId) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		vmm.destroyVm(vmId);
	}

	public void deleteVmsOfApp(String appId) {
		// TODO Auto-generated method stub
	}

	public String uploadImage(ImageToUpload imageInfo) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.uploadImage(imageInfo);
	}

	public void deleteImage(String id) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		vmm.destroyImage(id);
	}

	@Override
	public List<VmCost> getVMCosts(List<String> ids) {
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getCosts(ids);
	}
}
