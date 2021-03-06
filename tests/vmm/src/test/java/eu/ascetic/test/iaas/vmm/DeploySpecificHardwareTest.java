/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.iaas.vmm;

import es.bsc.vmmclient.models.*;
import eu.ascetic.test.conf.VMMConf;
import eu.ascetic.test.iaas.vmm.base.VmmTestBase;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
public class DeploySpecificHardwareTest  extends VmmTestBase{
    private static final Logger logger = Logger.getLogger("DeploySpecificHardware");
    
    public void testHwInfo() throws Exception {
        Map<String, HardwareInfo> hwinfo = vmm.getHardwareInfo();
        
        String hostname = "";
        String cpuExpectedModel = "";
        if(environment.equals("dev")){
            hostname = "compute1";
            cpuExpectedModel = "Intel(R) Core(TM) i7-4600U CPU @ 2.10GHz";
        } else if(environment.equals("test")){
            hostname = "wally152";
            cpuExpectedModel = "Intel Xeon E312xx (Sandy Bridge)";
        } else if(environment.equals("stable")){
            hostname = "wally158";
            cpuExpectedModel = "Intel Xeon E312xx (Sandy Bridge)";
        } else{
            throw new Exception("Environment must be defined on testHwInfo()");
        }
        
        HardwareInfo h1 = hwinfo.get(hostname);
        assertEquals("Intel", h1.getCpuVendor());
        assertEquals("x86_64", h1.getCpuArchitecture());
        assertEquals(cpuExpectedModel, h1.getCpuModel());
        assertEquals("SSD", h1.getDiskType());

        String cpuVendor = vmm.getHardwareInfo(hostname, "cpu", "vendor");
        assertEquals("Intel", cpuVendor);

        String cpuArch = vmm.getHardwareInfo(hostname, "cpu", "arch");
        assertEquals("x86_64", cpuArch);

        String diskType = vmm.getHardwareInfo(hostname, "disk", "type");
        assertEquals("SSD", diskType);
    }
    
    public void testSlots() throws Exception {
        List<Slot> slots = vmm.getSlots();
        assertTrue(slots.size() > 0);
    }

    public void testDeployCorrectHardware() throws Exception {
        String vmName = "deployCorrectHardwareTest01";
        VmRequirements vmDeployRequirements = 
            new VmRequirements( 1, 256, 1, 16, "x86_64", "Intel", "SSD");

        logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");
        Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla");
        List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
        VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();

        assertEquals("ACTIVE", vmd.getState());
    }
    
    public void testDeployWrongHardware() throws Exception {
        String vmName = "deployWrongHardwareTest01";
        logger.info("An exception is going to be thrown for testDeployWrongHardware(). This is expected.");
        try{
            VmRequirements vmDeployRequirements = 
                new VmRequirements( 1, 256, 1, 16, "x86_32", "AMD", "RAID");
            logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");

            Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla");
            List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
            VmDeployed vmd = vmm.getVm(deployedVms.get(0));
            vmId = vmd.getId();
            
            assertTrue(deployedVms.isEmpty());
        }
        catch(Exception e){
            e.printStackTrace();
            assertTrue(e.getMessage().contains("No suitable deployment plan found"));
        }
    }

    public void testDeployCorrectCpuBrand() throws Exception {
        String cpuBrand = "";
        if(environment.equals("dev")){
            cpuBrand = "Intel(R) Core(TM) i7-4600U CPU @ 2.10GHz";
        } else if(environment.equals("test")){
            cpuBrand = "Intel Xeon E312xx (Sandy Bridge)";
        } else if(environment.equals("stable")){
            cpuBrand = "Intel Xeon E312xx (Sandy Bridge)";
        } else{
            throw new Exception("Environment must be defined on testDeployCorrectCpuBrand()");
        }
        
        String vmName = "deployCorrectCpuBrandTest01";
        VmRequirements vmDeployRequirements = 
            new VmRequirements( 1, 256, 1, 16, "x86_64", "Intel", cpuBrand, "SSD");

        logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");
        Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla");
        List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
        VmDeployed vmd = vmm.getVm(deployedVms.get(0));
        vmId = vmd.getId();

        assertEquals("ACTIVE", vmd.getState());
    }

    public void testDeployWrongCpuBrand() throws Exception {
        String cpuBrand = "";
        if(environment.equals("dev")){
            cpuBrand = "Intel(R) Core(TM) i5-650 CPU @ 3.20GHz";
        } else if(environment.equals("test")){
            cpuBrand = "Intel Xeon E5-2403";
        } else if(environment.equals("stable")){
            cpuBrand = "Intel Xeon E5-2403";
        } else{
            throw new Exception("Environment must be defined on testDeployCorrectCpuBrand()");
        }
        
        logger.info("An exception is going to be thrown for testDeployWrongCpuBrand(). This is expected.");
        try{
            String vmName = "deployWrongCpuBrandTest01";
            VmRequirements vmDeployRequirements = 
                new VmRequirements( 1, 256, 1, 16, "x86_64", "Intel", cpuBrand, "SSD");

            logger.info("Deploying " + vmName + " with requirements:" + vmDeployRequirements.toString() + "...");
            Vm vm = new Vm(vmName, VMMConf.imageId, vmDeployRequirements, null, "dsh01", "", "sla");
            List<String> deployedVms = vmm.deployVms(Arrays.asList(vm));
            VmDeployed vmd = vmm.getVm(deployedVms.get(0));
            vmId = vmd.getId();
            
            assertTrue(deployedVms.isEmpty());
        }
        catch(Exception e){
            e.printStackTrace();
            assertTrue(e.getMessage().contains("No suitable deployment plan found"));
        }
    }
}
