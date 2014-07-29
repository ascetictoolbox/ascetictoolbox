package eu.ascetic.paas.applicationmanager.vmmanager;

import java.util.ArrayList;
import java.util.List;

import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageToUpload;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ImageUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListVmsDeployed;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.SchedulingAlgorithm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

public class VmManagerTest2 {
	
	private static String testingImageId="0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10";
	private static String testingImageUrl="http://cdn.download.cirros-cloud.net/0.3.1/cirros-0.3.1-x86_64-disk.img";
	private static String testingImageName="testingTestImageFromAppManager";
	private static String testingDeploymentBaseUrl="http://0.0.0.0:34372/vmmanager/";
	private static String testingVmDeployedId = "657368e2-e66d-48dc-8e06-aa3943123207";
	private static String testingVmNewStatus = "stop"; //Posible actions are "rebootHard", "rebootSoft", "start", "stop", "suspend", and "resume"
	
	public static void main(String[] args) {
		VmManagerClientHC client = new VmManagerClientHC("http://10.4.0.15:34372/vmmanager");

//		insertSeparator("testGetAllImages");
//		testGetAllImages(client);
//		testUploadImage(client);
//		insertSeparator("testGetVm");
//		testGetVm(client);
//		insertSeparator("testRemoveVm");
//		testRemoveVm(client);
//		insertSeparator("testGetAllVms");
//		testGetAllVms(client);
//		insertSeparator("testAddNewVm");
//		testAddNewVm(client);
//		insertSeparator("testGetImage");
//		testGetImage(client);
//		insertSeparator("testChangeStateVm");
//		testChangeStateVm(client);
		System.out.println();

		
	}
	
	
	public static void insertSeparator(String message){
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("***************  " + message + "  *********************");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
	}

	
	public static void testGetImage(VmManagerClientHC client){
		String imageId = "5fbdd9f8-67a8-4d08-af67-4918abdac4ef";
		ImageUploaded img = client.getImage(imageId);
		if (img != null){
			printImage(img);
		}
		else {
			System.out.println("Image with id = " + imageId + " cannot be finded in OpenStack");
		}
	}
	
	
	public static void testGetVm(VmManagerClientHC client){
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
	
	
	public static void testGetAllImages(VmManagerClientHC client){
		ListImagesUploaded list = client.getAllImages();
		
		if (list != null && !list.getImages().isEmpty()){
			int index = 0;
			for (ImageUploaded image : list.getImages()){
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
	
	
	public static void testGetAllVms(VmManagerClientHC client){
		ListVmsDeployed list = client.getAllVMs();
		
		if (list != null && !list.getVms().isEmpty()){
			int index = 0;
			for (VmDeployed vm : list.getVms()){
				System.out.println("VM " + index);
				printVm(vm);
				System.out.println();
				index++;
			}
		}
		else {
			System.out.println("No images available in OpenStack environment");
		}

	}
	
	
	public static void testUploadImage(VmManagerClientHC client){
		//upload image
		ImageToUpload image = new ImageToUpload(testingImageName, testingImageUrl);		
		String newId = client.uploadImage(image);

		System.out.println("new vm id = " + newId);
	}
	
	
	public static void testRemoveVm(VmManagerClientHC client){
		//delete vm
		String vmId = "";
		boolean deleted = client.destroyVM(vmId);
		if (deleted) {
			System.out.println("VM with id = " + vmId + " deleted successfully");
		}
		else {
			System.out.println("VM with id = " + vmId + " cannot be deleted");
		}
	}
	
	public static void testAddNewVm(VmManagerClientHC client){
		Vm vm = new Vm("testVmAppManager4", "0c6a0be4-38e5-4a99-bfc0-9cc32ab83e10", 1, 1024, 1, null , "");
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
	
	
	public static void testChangeStateVm(VmManagerClientHC client){
		
		boolean changed = client.changeStateVm(testingVmDeployedId, testingVmNewStatus);
		if (changed){
			System.out.println("Status of the vm with id = " + testingVmDeployedId + " switched to " + testingVmNewStatus);
		}
		else {
			System.out.println("Error. Status of the vm with id = " + testingVmDeployedId + " cannot be switched to " + testingVmNewStatus);
		}
		
	}

}
