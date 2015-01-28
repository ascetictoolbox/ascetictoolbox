package eu.ascetic.paas.applicationmanager.vmmanager.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

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
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net
 * 
 * Integration test for the VM Client with the ASCETiC VM Manager
 */
public class VmManagerClientHCIT {

	@Test
	public void getListOfImagesTest() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ListImagesUploaded imagesUploaded = vmManager.getAllImages();
		System.out.println("Images uploaded...: " + imagesUploaded.getImages().size());
	}
	
	@Test
	public void getImageTest() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ImageUploaded imageUploaded = vmManager.getImage("ce1483ab-0399-443e-9e25-3a2e77cf873f");
		System.out.println("Image name...: " + imageUploaded.getName());
	}
	
	@Test
	public void getAllVmsTest() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ListVmsDeployed listVmsDeployed = vmManager.getAllVMs();
		System.out.println("Total VMs Deployed...: " + listVmsDeployed.getVms().size());
	}
	
	@Test
	public void getVmTest() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		VmDeployed vmDeployed = vmManager.getVM("5bca1bfd-da97-4411-93e8-e35e8dcf2f07");
		System.out.println("Image name...: " + vmDeployed.getName());
	}
	
	@Test
	public void getVmsOfAAppTest() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ListVmsDeployed listVmsDeployed = vmManager.getVmsOfApp("JEPlus");
		
		System.out.println("Total VMs of a App Deployed...: " + listVmsDeployed.getVms().size());
	}
	
	@Test
	public void uploadAnImageAVMAndDeleteVMAndDeleteImage() {
		Configuration.vmManagerServiceUrl = "http://localhost:8080/vmmanager";
		
		VmManagerClient vmManager = new VmManagerClientHC();
		
		ImageToUpload imageToUpload = new ImageToUpload("jmeter.img", "/DFS/ascetic/vm-images/threeTierWebApp/jmeter.img");
		
		String imageHref = vmManager.uploadImage(imageToUpload);
		System.out.println("Image uploaded: " + imageHref);
		
		String suffix = "_test";
		Vm virtMachine = new Vm("JMeter" + suffix, imageHref, 1, 1024, 20, "initScript=/DFS/ascetic/vm-images/threeTierWebApp/jmeter.iso_1" , "DavidGIntegrationTest" );
		
		List<Vm> vms = new ArrayList<Vm>();
		vms.add(virtMachine);
		
		List<String> vmIds = vmManager.deployVMs(vms);
		
		VmDeployed vmDeployed = vmManager.getVM(vmIds.get(0));
		
		System.out.println("VM deployed: " + vmDeployed);
		
		boolean vmDeleted = vmManager.deleteVM(vmIds.get(0));
		
		System.out.println("Was the VM deleted? " + vmDeleted);
		
		boolean deleted = vmManager.deleteImage(imageHref);
		
		System.out.println("Was the image deleted? " + deleted);
	}
}
