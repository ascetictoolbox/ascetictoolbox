package eu.ascetic.paas.applicationmanager.vmmanager;

import java.util.ArrayList;
import java.util.List;

import es.bsc.vmmclient.models.ImageToUpload;
import es.bsc.vmmclient.models.ImageUploaded;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientBSSC;;


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
 */

public class VmManagerTest2IT {
	
	private static String testingImageUrl="http://cdn.download.cirros-cloud.net/0.3.1/cirros-0.3.1-x86_64-disk.img";
	private static String testingImageName="testingTestImageFromAppManager";
	private static String testingVmDeployedId = "26607a9a-3a42-4c8b-8fac-2d4fc18d3855";
	private static String testingVmNewStatus = "start"; //Posible actions are "rebootHard", "rebootSoft", "start", "stop", "suspend", and "resume"
	private static String testingAppId = "appManager";
	
	public static void main(String[] args) {
//		VmManagerClientHC client = new VmManagerClientHC("http://10.4.0.15:34372/vmmanager");
		VmManagerClientBSSC client = new VmManagerClientBSSC(Configuration.vmManagerServiceUrl);

//		insertSeparator("testGetAllImages");
//		testGetAllImages(client);
//		testUploadImage(client);
//		insertSeparator("testGetVm");
//		testGetVm(client);
//		insertSeparator("testDeleteVm");
//		testDeleteVm(client);
//		insertSeparator("testDeleteVmsOfApp");
//		testDeleteVmsOfApp(client);
//		insertSeparator("testGetAllVms");
		testGetAllVms(client);
//		insertSeparator("testDeployNewVm");
//		testDeployNewVm(client);
//		insertSeparator("testDeployNewVms");
//		testDeployNewVms(client);
//		insertSeparator("testGetImage");
//		testGetImage(client);
//		insertSeparator("testChangeStateVm");
//		testChangeStateVm(client);
//		insertSeparator("testGetVmsOfApp");
//		testGetVmsOfApp(client);
//		System.out.println();

		
	}
	
	
	


	public static void insertSeparator(String message){
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("***************  " + message + "  *********************");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
	}

	
	public static void testGetImage(VmManagerClientBSSC client){
		String imageId = "5fbdd9f8-67a8-4d08-af67-4918abdac4ef";
		ImageUploaded img = client.getImage(imageId);
		if (img != null){
			printImage(img);
		}
		else {
			System.out.println("Image with id = " + imageId + " cannot be finded in OpenStack");
		}
	}
	
	
	public static void testGetVm(VmManagerClientBSSC client){
		String vmId = "d46a9677-d26d-4faa-8e78-848762ba6159";
		VmDeployed vm = client.getVM(vmId);
		if (vm!=null){
			printVm(vm);	
		}
		else {
			System.out.println("Vm with id = " + vmId + " cannot be found in OpenStack");
		}
	}
	
	
	
	public static void printVm(VmDeployed vm){
		System.out.println("name: " + vm.getName());
		System.out.println("hostname: " + vm.getHostName());
		System.out.println("ID: " + vm.getId());
		System.out.println("appId: " + vm.getApplicationId());
		System.out.println("cpus: " + vm.getCpus());
		System.out.println("diskGb: " + vm.getDiskGb());
		System.out.println("image: " + vm.getImage());
		System.out.println("initScript: " + vm.getInitScript());
		System.out.println("ipAddress: " + vm.getIpAddress());
		System.out.println("ramMb: " + vm.getRamMb());
		System.out.println("state: " + vm.getState());
		System.out.println("created: " + vm.getCreated().toString());	
	}
	
	
	public static void printImage(ImageUploaded img){
		System.out.println("imageId: " + img.getId());
		System.out.println("status: " + img.getStatus());
		System.out.println("name: " + img.getName());
	}
	
	
	public static void testGetAllImages(VmManagerClientBSSC client){
		List<ImageUploaded> images = client.getAllImages();
		
		if (images != null && !images.isEmpty()){
			int index = 0;
			for (ImageUploaded image : images){
				System.out.println("IMAGE " + index);
				printImage(image);
				System.out.println();
				index++;
			}
		}
		else {
			System.out.println("No images available in OpenStack environment");
		}

	}
	
	
	public static void testGetAllVms(VmManagerClientBSSC client){
		List<VmDeployed> vms = client.getAllVMs();
		
		if (vms != null && !vms.isEmpty()){
			int index = 0;
			for (VmDeployed vm : vms){
				System.out.println("VM " + index);
				printVm(vm);
				System.out.println();
				index++;
			}
		}
		else {
			System.out.println("No VMs available in OpenStack environment");
		}

	}
	
	
	public static void testUploadImage(VmManagerClientBSSC client){
		//upload image
		ImageToUpload image = new ImageToUpload(testingImageName, testingImageUrl);		
		String newId = client.uploadImage(image);

		System.out.println("new vm id = " + newId);
	}
	
	
	public static void testDeleteVm(VmManagerClientBSSC client){
		//delete vm
		String vmId = testingVmDeployedId;
		client.deleteVM(vmId);
		System.out.println("VM with id = " + vmId + " deleted successfully");
	}
	
	public static void testDeleteVmsOfApp(VmManagerClientBSSC client) {
		client.deleteVmsOfApp(testingAppId);
		System.out.println("VmsOfApp with id = " + testingAppId + " deleted successfully");
	}
	
	
	
	public static void testDeployNewVm(VmManagerClientBSSC client) {
		Vm vm = new Vm("testVmAppManager", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, 256, null , testingAppId, "", "");
		List<Vm> listVm = new ArrayList<Vm>();
		listVm.add(vm);
		List<String> deployedVmsId = client.deployVMs(listVm);
		if (deployedVmsId != null && !deployedVmsId.isEmpty()){
			String strNewIds = "";
			for (String newId : deployedVmsId){
				strNewIds += newId + ", ";
			}
			System.out.println("Vms deployed successfully. New ids = " + strNewIds);
		}
		else {
			System.out.println("Error. Vms cannot be deployed");
		}		
	}
	
	
	public static void testChangeStateVm(VmManagerClientBSSC client){
		
		client.changeStateVm(testingVmDeployedId, testingVmNewStatus);
		
			System.out.println("State of the vm with id = " + testingVmDeployedId + " switched to " + testingVmNewStatus);
		
		
	}
	
	
	public static void testGetVmsOfApp(VmManagerClientBSSC client) {
		List<VmDeployed> vms = client.getVmsOfApp(testingAppId);
		

		if (vms != null && !vms.isEmpty()){
			int index = 0;
			for (VmDeployed vm : vms) {
				System.out.println("VM " + index);
				printVm(vm);
				System.out.println();
				index++;
			}
		}
		else {
			System.out.println("No VMs available in OpenStack environment for appId = " + testingAppId);
		}
	}
	
	
	public static void testDeployNewVms(VmManagerClientBSSC client){
		Vm vm = new Vm("testVmAppManager_00", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, 256, null , testingAppId, "", "");
		Vm vm1 = new Vm("testVmAppManager_01", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, 256, null , testingAppId, "", "");
		Vm vm2 = new Vm("testVmAppManager_02", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, 256, null , testingAppId, "", "");
		Vm vm3 = new Vm("testVmAppManager_04", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, 256, null , testingAppId, "", "");
		List<Vm> listVm = new ArrayList<Vm>();
		listVm.add(vm);
		listVm.add(vm1);
		listVm.add(vm2);
		listVm.add(vm3);
		List<String> deployedVmsId = client.deployVMs(listVm);
		if (deployedVmsId != null && !deployedVmsId.isEmpty()){
			String strNewIds = "";
			for (String newId : deployedVmsId){
				strNewIds += newId + ", ";
			}
			System.out.println("Vms deployed successfully. New ids = " + strNewIds);
		}
		else {
			System.out.println("Error. Vms cannot be deployed");
		}		
	}

}
