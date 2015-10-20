import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class D41TEST extends TestCase {

	private static final String URL_D41 = "http://iaas-stable:34373/vmmanager";
	private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
	private static final String URL_STABLE = "http://iaas-stable:34372/vmmanager";


	@Ignore
	public void testGetVMMInfo() {
		VmManagerClient vmm = new VmManagerClient(URL_D41);

		System.out.println("* Uploaded images");
		for(ImageUploaded img : vmm.getImages()) {
			System.out.println("\t"+img.toString());
		}
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


//
//		System.out.println("* deploying new vms");
//			// deploy vms
//		List<Vm> toDeploy = new ArrayList<>();
//		for(int i = 1 ; i <= 4 ; i++) {
//			Vm vm = new Vm("d41vm"+i,
//					"db040522-e744-40e2-af4d-e9ee6c21f18b",
//					2,1024,3,512,null,null,null,null,false);
//
//			toDeploy.add(vm);
//		}
//
//		List<String> vmIds = vmm.deployVms(toDeploy);
//		for(String id : vmIds) {
//			System.out.println("\tdeployed: " + vmm.getVm(id).toString());
//		}

	}

	@Ignore
	public void testScratch() {
		VmManagerClient vmm = new VmManagerClient("http://iaas-test:34372/vmmanager");

		for(VmCost vc : vmm.getCosts(Arrays.asList(
				"1c80ec76-4827-4616-b042-46441ac2acc2","d3ed569c-89c8-470e-b0a2-9f051115f3e3"))) {
			System.out.println("vc = " + vc);
		}

	}
}
