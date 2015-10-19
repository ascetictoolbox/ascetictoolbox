import es.bsc.vmmclient.models.ImageUploaded;
import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.VmCost;
import es.bsc.vmmclient.models.VmDeployed;
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

		System.out.println("* Uploaded images");
		for(ImageUploaded img : vmm.getImages()) {
			System.out.println("\t"+img.toString());
		}

		System.out.println("* Nodes");
		for(Node node : vmm.getNodes()) {
			System.out.println("\t"+node.toString());
		}

		System.out.println("* Deployed vms");
		for(VmDeployed vm : vmm.getVms()) {
			System.out.println("\t"+vm.toString());
		}

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
