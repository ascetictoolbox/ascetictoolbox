
import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.*;

public class D41TEST extends TestCase {

	private static final String URL_D41 = "http://iaas-stable:34373/vmmanager";
	private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
	private static final String URL_STABLE = "http://iaas-stable:34372/api/v1";
    private static final String URL_LOCAL = "http://localhost:34372/api/v1";

	private static final int NUMBER_OF_VMS = 1;
	//	private static final String CIRROS_IMAGE_ID = "0c29c65b-2ff8-46fc-acd7-fdb039316905"; //iaas testing
	private static final String CIRROS_IMAGE_ID = "d967c216-cbc5-4dc7-b197-cc2a4e0752f8"; //iaas stable


	VmManagerClient vmm;
	@Override
	public void setUp() throws Exception {
		super.setUp();
		vmm = new VmManagerClient(URL_LOCAL);
	}

	@Ignore
	public void testAll() {
		testShowUploadedImages();
		testShowNodesAndVms();
		try {
			Thread.sleep(1*60*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		testConsolidationScenario();
		//testDeleteVMs();
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
		for (VmDeployed vm : getVMsFromThisTest()) {
			System.out.println("deleting " + vm.getName() + "... ");
			if(vm.getName().startsWith("d41vm")) {
				vmm.destroyVm(vm.getId());
			}
		}
	}

	@Ignore
	public void testConsolidationScenario() {

		long WAIT_MS = 4 * 60 * 1000; // every 5 minutes

		List<String> hosts = new ArrayList<>(Arrays.asList("wally159","wally162","wally163"));
		int hostIndex = 2;

//		testDeleteVMs();
//		testDeployVms();

		// eliminate VMs ordered by HOST (so always we are deleting)
		List<VmDeployed> deployed = getVMsFromThisTest();
		Collections.sort(deployed, new VmDeployedComparatorByHost());

		boolean first = true;
		while(deployed.size() > 3) {

			if(first) {
				first = false;
			} else {
				try {
					Thread.sleep(WAIT_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			deployed = getVMsFromThisTest();
			Collections.sort(deployed,new VmDeployedComparatorByHost());
			for (VmDeployed v : deployed) {
				System.out.println(v.getName() + "\t" + v.getHostName());
			}

			VmDeployed vmToDestroy = null;
			boolean found = false;
			while(!found) {
				String hostToDestroy = hosts.get(hostIndex);
				for(VmDeployed vm : deployed) {
					if(hostToDestroy.equalsIgnoreCase(vm.getHostName())) {
						found = true;
						vmToDestroy = vm;
						break;
					}
				}
				hostIndex = (hostIndex + 1) % hosts.size();
			}
			System.out.println("Destroying " + vmToDestroy.getName() + " from " + vmToDestroy.getHostName());
			vmm.destroyVm(vmToDestroy.getId());
		}
	}

	private List<VmDeployed> getVMsFromThisTest() {
		List<VmDeployed> all = vmm.getVms();
		ArrayList<VmDeployed> test = new ArrayList<>();
		for(VmDeployed a : all) {
			if(a.getName().startsWith("d41vm")) {
				test.add(a);
			}
		}
		return test;
	}

	private class VmDeployedComparatorByHost implements Comparator<VmDeployed> {
		@Override
		public int compare(VmDeployed o1, VmDeployed o2) {
			return o1.getHostName().compareTo(o2.getHostName());
		}
	}

}


//d41vm12	wally159
//d41vm11	wally159
//d41vm7	wally159
//d41vm4	wally159
//d41vm9	wally162
//d41vm8	wally162
//d41vm5	wally162
//d41vm3	wally162
//d41vm10	wally163
//d41vm6	wally163
//d41vm2	wally163
//d41vm1	wally163
//Destroying d41vm12 from wally159
//d41vm11	wally159
//d41vm7	wally159
//d41vm4	wally159
//d41vm9	wally162
//d41vm8	wally162
//d41vm5	wally162
//d41vm3	wally162
//d41vm10	wally163
//d41vm6	wally163
//d41vm2	wally163
//d41vm1	wally163
//Destroying d41vm9 from wally162