import es.bsc.vmmclient.models.VmCost;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

public class ScratchTest extends TestCase {
	@Ignore
	public void testScratch() {
		VmManagerClient vmm = new VmManagerClient("http://iaas-test:34372/vmmanager");

		for(VmCost vc : vmm.getCosts(Arrays.asList(
				"8eb729dc-6d17-454c-a0ae-deab5f35280a",
				"467b6957-491a-4419-b6ef-21834f2b25c4"))) {
			System.out.println("vc = " + vc);
		}

	}
}
