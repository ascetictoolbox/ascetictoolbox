package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.*;
import eu.ascetic.test.conf.VMMConf;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
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
public class VerticalScalabilityTest extends VmmTestBase{
    private static final Logger logger = Logger.getLogger("VerticalScalabilityTest");
    
    public void testNotSupportedOnTestbed() throws Exception {
        assertTrue(true);
    }

    /*public void testDeployAndScaleConfirmManually() throws Exception {
        VmRequirements vmDeployRequirements = new VmRequirements( 1, 256, 1, 16);
        
        VmRequirements vmScaleRequirements = new VmRequirements( 2, 512, 2, 32);
        vmScaleRequirements.setAutoConfirm(false);
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
            if( environment.equals("dev") || 
                (node.matchesRequirements(vmDeployRequirements) &&
                node.matchesRequirements(vmScaleRequirements)) ){
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
        if(vmm.getVm(vmId).getState().equals("VERIFY_RESIZE")){
            vmm.confirmResize(vmId);
        }
        
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
    }*/
    
    /*public void testDeployAndScaleAutoConfirm() throws Exception {
        VmRequirements vmDeployRequirements = new VmRequirements( 1, 256, 1, 16);
        
        VmRequirements vmScaleRequirements = new VmRequirements( 2, 512, 2, 32);
        vmScaleRequirements.setAutoConfirm(true);
        
        List<Node> nodes = new ArrayList<Node>();
        for (Node node : vmm.getNodes()) {
            if( environment.equals("dev") || 
                (node.matchesRequirements(vmDeployRequirements) &&
                node.matchesRequirements(vmScaleRequirements)) ){
                nodes.add(node);
            }
		}
        
        assertTrue("Can't run test with less than 1 compute nodes with enough resources!", 
            nodes.size() >= 1);
        
        String vmName = "deployAndScaleTest02";
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
        
        VmDeployed vmScalated = vmm.getVm(vmId);
        assertEquals("ACTIVE", vmScalated.getState());
        assertEquals(vmScaleRequirements.getCpus(), vmScalated.getCpus());
        assertEquals(vmScaleRequirements.getDiskGb(), vmScalated.getDiskGb());
        assertEquals(vmScaleRequirements.getRamMb(), vmScalated.getRamMb());
        assertEquals(vmScaleRequirements.getSwapMb(), vmScalated.getSwapMb());
    }*/
}