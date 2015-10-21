import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class D41TEST extends TestCase {

	private static final String URL_D41 = "http://iaas-stable:34373/vmmanager";
	private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
	private static final String URL_STABLE = "http://iaas-stable:34372/vmmanager";

	VmManagerClient vmm;
	@Override
	public void setUp() throws Exception {
		super.setUp();
		vmm = new VmManagerClient(URL_D41);
	}

	@Ignore
	public void testAll() {
		testShowUploadedImages();
		testShowNodesAndVms();
		testDeleteVMs();
		testDeployVms();
	}


	@Ignore
	public void testShowUploadedImages() {
		System.out.println("* Uploaded images");
		for (ImageUploaded img : vmm.getImages()) {
			System.out.println("\t" + img.toString());
		}
	}
	@Ignore
	public void testShowNodesAndVms() {
		System.out.println("* Nodes");
		for (Node node : vmm.getNodes()) {
			System.out.println("\t" + node.toString());
		}
		System.out.println("* Deployed vms");
		for(VmDeployed vm : vmm.getVms()) {
			System.out.println("\t"+vm.toString());
		}
	}

	@Ignore
	public void testDeleteVMs() {
		System.out.println("* deleting all vms");
		for (VmDeployed vm : vmm.getVms()) {
			System.out.println("deleting " + vm.getName() + "... ");
			vmm.destroyVm(vm.getId());
		}
	}

	@Ignore
	public void testDeployVms() {

		System.out.println("* deploying new vms");
			// deploy vms
		List<Vm> toDeploy = new ArrayList<>();
		for(int i = 1 ; i <= 12 ; i++) {
			Vm vm = new Vm("d41vm"+i,
					"d967c216-cbc5-4dc7-b197-cc2a4e0752f8",
					2,1024,1,6*512,
					"#!/bin/sh\n" +
							"\n" +
							"forkfunc() {\n" +
							"\ta=2;\n" +
							"\tb=3;\n" +
							"\n" +
							"\twhile [ 1 == 1 ]\n" +
							"\tdo\n" +
							"\t\ta=`expr '(' '(' $a '+' '1' ')' '*' $b ')' '/' $a`\n" +
							"\t\tb=`expr '(' $b '*' $b ')'`\n" +
							"\n" +
							"\t\tif [ \"$a\" -ge 100000 ]\n" +
							"\t\tthen\n" +
							"\t\t\ta=2\n" +
							"\t\tfi\n" +
							"\t\tif [ \"$b\" -ge 100000 ]\n" +
							"\t\tthen\n" +
							"\t\t\tb=3\n" +
							"\t\tfi\n" +
							"\n" +
							"\t\techo \"hola tio $a $b\"\n" +
							"\tdone\n" +
							"}\n" +
							"\n" +
							"forkfunc & forkfunc & forkfunc & forkfunc",
					"test-id",null,null,true);

			toDeploy.add(vm);
		}

		List<String> vmIds = vmm.deployVms(toDeploy);
		for(String id : vmIds) {
			VmDeployed vmDeployed = vmm.getVm(id);
			System.out.println("\t" + vmDeployed.getIpAddress() +" : " + vmDeployed.toString());
		}

	}

	@Ignore
	public void testDistributionScenario() {
		testDeleteVMs();
		testDeployVms();

		// eliminate VMs ordered by HOST (so always we are deleting)
//		Set<VmDeployed>
//

		String nodeToDelete = "wally162";

		for(VmDeployed vm : vmm.getVms()) {
			if(nodeToDelete.equals(vm.getHostName())) {
				System.out.println("\tDeleting " + vm.getIpAddress());
				vmm.destroyVm(vm.getId());
			}
		}
	}

}
