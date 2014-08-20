package eu.ascetic.paas.applicationmanager.vmmanager;

import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.ListImagesUploaded;

public class VmManagerTestIT {

	public static void main(String[] args) {
		VmManagerClientHC client = new VmManagerClientHC();
		ListImagesUploaded list = client.getAllImages();
//		ListVmEstimates listVmEstimates = client.getListOfVmEstimates();
		System.out.println();
	}

}
