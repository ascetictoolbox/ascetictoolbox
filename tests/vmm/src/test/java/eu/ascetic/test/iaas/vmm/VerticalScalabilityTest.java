package eu.ascetic.test.iaas.vmm;

import es.bsc.demiurge.core.models.vms.VmRequirements;
import es.bsc.vmmclient.models.Node;
import es.bsc.vmmclient.models.Vm;
import es.bsc.vmmclient.models.VmDeployed;
import es.bsc.vmmclient.vmm.VmManagerClient;
import eu.ascetic.test.conf.VMMConf;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * @author Raimon Bosch (http://github.com/raimonbosch)
 */
public class VerticalScalabilityTest extends VmmTestBase{
    private static final Logger logger = Logger.getLogger("VerticalScalabilityTest");

    public void testDeployAndScale() throws Exception {
        VmRequirements vmDeployRequirements = new VmRequirements( 1, 256, 1, 16);
        VmRequirements vmScaleRequirements = new VmRequirements( 2, 512, 2, 32);
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
			if( node.matchesRequirements(vmDeployRequirements) &&
                node.matchesRequirements(vmScaleRequirements)){
                nodes.add(node);
            }
		}
        
        assertTrue("Can't run test with less than 1 compute nodes with enough resources!", 
            nodes.size() >= 1);
        
        String vmName = "deployAndScaleTest01";
        String computeNode01 = nodes.get(0).getHostname();
        
        //Deploy
        logger.info("Deploying '" + vmName + "' at " + computeNode01 + "...");
        Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dst01", "", "sla", computeNode01);
		List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
		VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();
        
        assertEquals("ACTIVE", vmd.getState());
        assertEquals(computeNode01, vmd.getHostName());
        assertEquals(vmDeployRequirements.getCpus(), vmd.getCpus());
        assertEquals(vmDeployRequirements.getDiskGb(), vmd.getDiskGb());
        assertEquals(vmDeployRequirements.getRamMb(), vmd.getRamMb());
        assertEquals(vmDeployRequirements.getSwapMb(), vmd.getSwapMb());
        
        //Scale
        logger.info("Deployed " + vmName + " with id:" + vmId + 
            ". Scaling VM to " + computeNode01 + " with ...");
        vmm.resize(vmId, vmScaleRequirements);
        
        VmDeployed vmScalated = null;
        while(vmm.getVm(vmId).getState().equals("RESIZE") && loopIsAlive()) {
            logger.info("Waiting migration (resize) to finish...");
            Thread.sleep(2500);
        }
        
        //Scale confirmation
        vmm.confirmResize(vmId);
        
        while(vmm.getVm(vmId).getState().equals("VERIFY_RESIZE") && loopIsAlive()) {
            logger.info("Waiting confirmResize to finish...");
            Thread.sleep(2500);
        }
        
        vmScalated = vmm.getVm(vmId);
        assertEquals("ACTIVE", vmScalated.getState());
        assertEquals(vmScaleRequirements.getCpus(), vmScalated.getCpus());
        assertEquals(vmScaleRequirements.getDiskGb(), vmScalated.getDiskGb());
        assertEquals(vmScaleRequirements.getRamMb(), vmScalated.getRamMb());
        assertEquals(vmScaleRequirements.getSwapMb(), vmScalated.getSwapMb());
    }
}