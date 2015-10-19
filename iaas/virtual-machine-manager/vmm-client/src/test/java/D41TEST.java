import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

public class D41TEST extends TestCase {

	private static final String URL_D41 = "http://iaas-stable:34373/vmmanager";
	private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
	private static final String URL_STABLE = "http://iaas-stable:34372/vmmanager";


	@Ignore
	public void testGetVMMInfo() {
		VmManagerClient vmm = new VmManagerClient(URL_D41);

//		System.out.println("* Uploaded images");
//		for(ImageUploaded img : vmm.getImages()) {
//			System.out.println("\t"+img.toString());
//		}
//
//		System.out.println("* Nodes");
//		for(Node node : vmm.getNodes()) {
//			System.out.println("\t"+node.toString());
//		}

		System.out.println("* Deployed vms");
		for(VmDeployed vm : vmm.getVms()) {
			System.out.println("\t"+vm.toString());
		}

		System.out.println("* deleting all vms");
		for(VmDeployed vm : vmm.getVms()) {
			System.out.println("deleting " + vm.getName() + "... ");
			vmm.destroyVm(vm.getId());
		}


			// deploy vms
//		for(int i = 1 ; i <= 4 ; i++) {
//			System.out.println("deploying vm " + i);
//			Vm vm = new Vm("d41vm"+i,
//					"db040522-e744-40e2-af4d-e9ee6c21f18b",
//					2,1024,512,512,null,null,null,null,false);
//
//			List<String> vmId = vmm.deployVms(Arrays.asList(vm));
//
//			VmDeployed d = vmm.getVm(vmId.get(0));
//			System.out.println("\tdeployed: " + d.toString());
//		}

	}

//	@Ignore
//	public void testScratch() {
//		VmManagerClient vmm = new VmManagerClient("http://iaas-stable:34373/vmmanager");
//
//		for(VmCost vc : vmm.getCosts(Arrays.asList(
//				"8eb729dc-6d17-454c-a0ae-deab5f35280a",
//				"467b6957-491a-4419-b6ef-21834f2b25c4"))) {
//			System.out.println("vc = " + vc);
//		}
//
//	}
}
