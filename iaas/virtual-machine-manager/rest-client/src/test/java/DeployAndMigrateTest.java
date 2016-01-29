import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.vmm.VmManagerClient;
import junit.framework.TestCase;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mario Macías (http://github.com/mariomac)
 */
public class DeployAndMigrateTest extends TestCase{
    private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
    private static final String URL_STABLE = "http://iaas-stable:34372/vmmanager";

	private static final String URL_BSCGRID = "http://bscgrid28.bsc.es:34372/api/v1";

    private static final String IMAGE_ID = "bef15a10-64b9-4c5d-9ba7-0c29edbbe7b4"; // CirrOS at bscgrid28

    VmManagerClient vmm;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmm = new VmManagerClient(null); //URL_BSCGRID);
    }

    @Ignore
    public void testVmDeployment() throws InterruptedException {

		Vm vm = new Vm("MustBeInBSCGRID30", IMAGE_ID, 2,1024,1,6*512,null,"testSlaAppId", "", "sla", "bscgrid30");
		List<String> id1 = vmm.deployVms(Arrays.asList(vm));
		VmDeployed vmd = vmm.getVm(id1.get(0));
		System.out.println("deployed: " + vmd.toString());

		vm = new Vm("MustBeInBSCGRID28", IMAGE_ID, 2,1024,1,6*512,null,"testSlaAppId", "", "sla", "bscgrid28");
		List<String> id2 = vmm.deployVms(Arrays.asList(vm));
		vmd = vmm.getVm(id2.get(0));
		System.out.println("deployed: " + vmd.toString());

		List<Node> nodes = vmm.getNodes();
		for(Node n : nodes) {
			System.out.println("n.toString() = " + n.toString());
		}

		vmm.migrate(id1.get(0), "bscgrid31");
		vmm.migrate(id2.get(0), "bscgrid29");

		nodes = vmm.getNodes();
		for(Node n : nodes) {
			System.out.println("n.toString() = " + n.toString());
		}


    }
}
