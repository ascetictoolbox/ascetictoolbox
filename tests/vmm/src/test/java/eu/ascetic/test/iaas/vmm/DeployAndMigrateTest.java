package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.vmm.VmManagerClient;
import eu.ascetic.test.conf.VMMConf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * @author Raimon Bosch (http://github.com/raimonbosch), 
 *         Mario Macías (http://github.com/mariomac)
 */
public class DeployAndMigrateTest extends TestCase{
    private static final Logger logger = Logger.getLogger("DeployAndMigrateTest");
    
    VmManagerClient vmm;
    String vmId = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmm = new VmManagerClient(VMMConf.vmManagerURL);
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(vmId != null){
            //Destroy
            logger.info("Destroying VM " + vmId);
            vmm.destroyVm(vmId);

            VmDeployed vmDestroyed = vmm.getVm(vmId);
            assertNull(vmDestroyed);
            
            vmId = null;
        }
    }

    public void testDeployMigrateAndDestroy() throws Exception {
        int cpus = 1;
        int ramMb = 256;
        int swapMb = 16;
        int diskGb = 1;
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
			if( (node.getTotalCpus() - node.getAssignedCpus()) >= cpus && 
                (node.getTotalMemoryMb() - node.getAssignedMemoryMb()) >= ramMb && 
                (node.getTotalDiskGb() - node.getAssignedDiskGb()) >= diskGb){
                nodes.add(node);
            }
		}
        
        assertTrue("Can't run test with less than 2 compute nodes with enough resources!", 
            nodes.size() >= 2);
        
        String vmName = "deployAndMigrateTest01";
        String computeNode01 = nodes.get(0).getHostname();
        String computeNode02 = nodes.get(1).getHostname();
        
        //Deploy
        logger.info("Deploying '" + vmName + "' at " + computeNode01 + "...");
        Vm vm = new Vm("deployAndMigrateTest01", VMMConf.imageId, cpus, ramMb, diskGb, swapMb, 
                null, "dmt01", "", "sla", computeNode01);
		List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
		VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();
        
        assertEquals("ACTIVE", vmd.getState());
        assertEquals(computeNode01, vmd.getHostName());
        assertEquals(cpus, vmd.getCpus());
        assertEquals(diskGb, vmd.getDiskGb());
        assertEquals(ramMb, vmd.getRamMb());
        assertEquals(swapMb, vmd.getSwapMb());
        
        //Migrate
        logger.info("Deployed " + vmName + " with id:" + vmId + 
            ". Migrating VM to " + computeNode02 + "...");
        vmm.migrate(deployedVms.get(0), computeNode02);
        
        VmDeployed vmMigrated = null;
        while(vmm.getVm(vmId).getState().equals("MIGRATING")) {
            logger.info("Waiting migration to finish...");
            Thread.sleep(2500);
        }
        
        vmMigrated = vmm.getVm(vmId);
        assertEquals("ACTIVE", vmMigrated.getState());
        assertEquals(computeNode02, vmMigrated.getHostName());
        assertEquals(cpus, vmMigrated.getCpus());
        assertEquals(diskGb, vmMigrated.getDiskGb());
        assertEquals(ramMb, vmMigrated.getRamMb());
        assertEquals(swapMb, vmMigrated.getSwapMb());
    }
}