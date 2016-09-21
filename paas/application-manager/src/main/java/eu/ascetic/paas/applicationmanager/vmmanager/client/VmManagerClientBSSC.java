package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.List;

import org.apache.log4j.Logger;

import es.bsc.vmmclient.models.ImageToUpload;
import es.bsc.vmmclient.models.ImageUploaded;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.models.VmCost;


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
	
	public VmManagerClientBSSC(String url) {
		this.url = url;
		logger.info("Connecting to URL for VMM: " + url);
	}

	public String getURL() {
		return url;
	}

	public void setURL(String url) {
		this.url = url;
	}

	public List<ImageUploaded> getAllImages() {
		logger.info("Requesting a list of images. Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getImages();
	}

	public ImageUploaded getImage(String id) {
		logger.info("Getting an image: " + id  + ". Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getImage(id);
	}

	public List<VmDeployed> getAllVMs() {
		logger.info("Getting all VMse. Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getVms();
	}

	public VmDeployed getVM(String id) {
		logger.info("Getting a VM "  + id + ". Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getVm(id);
	}

	public List<VmDeployed> getVmsOfApp(String appId) {
		logger.info("Getting al VMs of an App "  + appId + ". Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getAppVms(appId);
	}

	public List<String> deployVMs(List<Vm> vms) {
		logger.info("Deploying a group of VMS. Connecting to URL for VMM: " + url);
		for(Vm vm : vms) {
			logger.info("PRINTING VM: ");
			logger.info("  Application Id  - " + vm.getApplicationId());
			logger.info("  CPUs - " + vm.getCpus());
			logger.info("  Disk GB -  " + vm.getDiskGb());
			logger.info("  Disk Type -  " + vm.getDiskType());
			logger.info("  Image -  " + vm.getImage());
			logger.info("  Init Script -  " + vm.getInitScript());
			logger.info("  Name -  " + vm.getName());
			logger.info("  OVF ID -  " + vm.getOvfId());
			logger.info("  Prefered Host -  " + vm.getPreferredHost());
			logger.info("  Processor ARchitecture -  " + vm.getProcessorArchitecture());
			logger.info("  Processor Brand -  " + vm.getProcessorBrand());
			logger.info("  Processor Model -  " + vm.getProcessorModel());
			logger.info("  RAM MB -  " + vm.getRamMb());
			logger.info("  SLA ID -  " + vm.getSlaId());
			logger.info("  SWAP MB -  " + vm.getSwapMb());
		}
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.deployVms(vms);
	}

	public void changeStateVm(String vmId, String action) {
		// TODO Auto-generated method stub

	}

	public void deleteVM(String vmId) {
		logger.info("Deleting a VM " + vmId + ". Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		vmm.destroyVm(vmId);
	}

	public void deleteVmsOfApp(String appId) {
		// TODO Auto-generated method stub
	}

	public String uploadImage(ImageToUpload imageInfo) {
		logger.info("Uploading an image. Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.uploadImage(imageInfo);
	}

	public void deleteImage(String id) {
		logger.info("Deleting an image: " + id + ". Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		vmm.destroyImage(id);
	}

	@Override
	public List<VmCost> getVMCosts(List<String> ids) {
		logger.info("Getting costs for several VMs. Connecting to URL for VMM: " + url);
		es.bsc.vmmclient.vmm.VmManagerClient vmm = new es.bsc.vmmclient.vmm.VmManagerClient(url);
		return vmm.getCosts(ids);
	}
}
