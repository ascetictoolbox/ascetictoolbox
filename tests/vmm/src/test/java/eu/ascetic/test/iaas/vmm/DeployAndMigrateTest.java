package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.*;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import es.bsc.vmmclient.models.Node;
import eu.ascetic.test.conf.SlotAwareDeployer;
import eu.ascetic.test.conf.SlotSolution;
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
    
    public void testSlotAwareDeployment() {
        /*String imageId= "01265289-873c-4d9f-a990-43d21e395739";
        VmRequirements vmRequirements = 
            new VmRequirements( 2, 2048, 10, 0);
        
        Vm vm1 = new Vm("slotAwareTest01", imageId, vmRequirements, null, "slotAwareTest", "", "sla", "bscgrid30");
        Vm vm2 = new Vm("slotAwareTest02", imageId, vmRequirements, null, "slotAwareTest", "", "sla", "bscgrid30");
        Vm vm3 = new Vm("slotAwareTest03", imageId, vmRequirements, null, "slotAwareTest", "", "sla", "bscgrid30");
        Vm vm4 = new Vm("slotAwareTest04", imageId, vmRequirements, null, "slotAwareTest", "", "sla", "bscgrid29");
        Vm vm5 = new Vm("slotAwareTest05", imageId, vmRequirements, null, "slotAwareTest", "", "sla", "bscgrid29");
        
        List<String> deployedVms = vmm.deployVms(Arrays.asList(vm1, vm2, vm3, vm4, vm5));
	
        VmDeployed vmd1 = vmm.getVm(deployedVms.get(0));
        VmDeployed vmd2 = vmm.getVm(deployedVms.get(1));
        VmDeployed vmd3 = vmm.getVm(deployedVms.get(2));
        VmDeployed vmd4 = vmm.getVm(deployedVms.get(3));
        VmDeployed vmd5 = vmm.getVm(deployedVms.get(4));
        vmIds.add(vmd1.getId());
        vmIds.add(vmd2.getId());
        vmIds.add(vmd3.getId());
        vmIds.add(vmd4.getId());
        vmIds.add(vmd5.getId());*/
        
        //System.out.println( vmm.getNodes() );
        //System.out.println( vmm.getSlots() );
        //System.out.println( vmm.getHardwareInfo() );
        
        List<Slot> slots = new ArrayList<>();
        slots.add(new Slot("hostA", 2, 200, 2000));
        slots.add(new Slot("hostB", 4, 400, 4000));
        slots.add(new Slot("hostC", 8, 800, 8000));

        int minCpus = 2;
        int maxCpus = 4;
        int totalCpusToAdd = 6;
        int cpusPerHost = 8;

        SlotAwareDeployer deployer = new SlotAwareDeployer();
        List<SlotSolution> solutions = deployer.getSlotsSortedByConsolidationScore(slots, totalCpusToAdd, cpusPerHost, minCpus, maxCpus, 1000, 100);
        System.out.println(solutions);
    }
}