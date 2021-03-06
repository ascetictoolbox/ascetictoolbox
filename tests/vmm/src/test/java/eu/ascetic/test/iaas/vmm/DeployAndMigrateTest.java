package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.*;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import es.bsc.vmmclient.models.Node;
import eu.ascetic.test.conf.VMMConf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class DeployAndMigrateTest extends VmmTestBase{
    private static final Logger logger = Logger.getLogger("DeployAndMigrateTest");
    
    public void testFloatingIpDeploy() throws Exception {
        int cpus = 1;
        int ramMb = 256;
        int diskGb = 1;
        int swapMb = 0;
        
        VmRequirements vmDeployRequirements = 
            new VmRequirements( cpus, ramMb, diskGb, swapMb);
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
			if( environment.equals("dev") || node.matchesRequirements(vmDeployRequirements)){
                nodes.add(node);
            }
		}
        
        assertTrue("Can't run test with less than 2 compute nodes with enough resources!", 
            nodes.size() >= 1);
        
        String vmName = "deployFloatingIpTest01";
        String computeNode01 = nodes.get(0).getHostname();
        
        //Deploy
        logger.info("Deploying '" + vmName + "' at " + computeNode01 + "...");
        Vm vm = new Vm("deployFloatingIpTest01", VMMConf.imageId, vmDeployRequirements, null, "dmt01", "", "sla", computeNode01, true);
		List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
		VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();
        assertTrue(vmd.getIpAddress().contains("192.168"));
    }

    public void testDeployAndMigrate() throws Exception {
        int cpus = 1;
        int ramMb = 256;
        int diskGb = 1;
        int swapMb = 0;
        
        VmRequirements vmMigrateRequirements = 
            new VmRequirements( cpus, ramMb, diskGb, swapMb);
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
			if( environment.equals("dev") || node.matchesRequirements(vmMigrateRequirements)){
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
        Vm vm = new Vm("deployAndMigrateTest01", VMMConf.imageId, vmMigrateRequirements, null, "dmt01", "", "sla", computeNode01);
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
        while(vmm.getVm(vmId).getState().equals("MIGRATING") && loopIsAlive()) {
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