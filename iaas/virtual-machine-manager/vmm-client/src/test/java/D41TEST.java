import es.bsc.vmmclient.models.ImageUploaded;
import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.VmCost;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

public class D41TEST extends TestCase {

	@Ignore
	public void getVMMInfo() {
		VmManagerClient vmm = new VmManagerClient("http://iaas-stable:34373/vmmanager");

		System.out.println("* Uploaded images");
		for(ImageUploaded img : vmm.getImages()) {
			System.out.println("\t"+img.toString());
		}

		System.out.println("* Nodes");
		for(Node node : vmm.getNodes()) {
			//sout
		}

	}

	@Ignore
	public void testScratch() {
		VmManagerClient vmm = new VmManagerClient("http://iaas-stable:34373/vmmanager");

		for(VmCost vc : vmm.getCosts(Arrays.asList(
				"8eb729dc-6d17-454c-a0ae-deab5f35280a",
				"467b6957-491a-4419-b6ef-21834f2b25c4"))) {
			System.out.println("vc = " + vc);
		}

	}
}
