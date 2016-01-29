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
public class SLATest extends TestCase{
    private static final String URL_D41 = "http://iaas-stable:34373/vmmanager";
    private static final String URL_TESTING = "http://192.168.3.17:34372/vmmanager/";
    private static final String URL_STABLE = "http://iaas-stable:34372/vmmanager";

    private static final String SLA_ID = "641bfdc1-528b-494d-9fe2-aa023ce2ec51";
    private static final String OVF_ID = "VM_of_type_ubu1";

    private static final String IMAGE_ID = "0c29c65b-2ff8-46fc-acd7-fdb039316905"; // CirrOS at testing

    VmManagerClient vmm;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmm = new VmManagerClient(URL_TESTING);
    }

    @Ignore
    public void testVmDeployment() {
        Vm vm = new Vm("testSlaVm", IMAGE_ID, 2,1024,1,6*512,null,"testSlaAppId", OVF_ID, SLA_ID );
        List<String> id = vmm.deployVms(Arrays.asList(vm));
        VmDeployed vmd = vmm.getVm(id.get(0));

        System.out.println("deployed: " + vmd.toString());
    }
}
