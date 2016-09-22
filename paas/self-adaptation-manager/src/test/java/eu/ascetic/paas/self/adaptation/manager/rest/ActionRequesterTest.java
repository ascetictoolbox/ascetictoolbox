/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.rest;

import eu.ascetic.paas.applicationmanager.model.VM;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import es.bsc.vmmclient.models.Slot;

/**
 * A test class for the rest interface to the Application Manager.
 *
 * @author Richard Kavanagh
 */
public class ActionRequesterTest {

    /**
     * This prints out a VM object to standard out
     *
     * @param vm
     */
    private static void printVM(VM vm) {
        System.out.println("---- VM ----");
        System.out.println("VM ID: " + vm.getId());
        System.out.println("IP: " + vm.getIp());
        System.out.println("Status: " + vm.getStatus());
        System.out.println("CPU Min: " + vm.getCpuMin()
                + " Max: " + vm.getCpuMax()
                + " Actual: " + vm.getCpuActual());
        System.out.println("OVF ID: " + vm.getOvfId());
        System.out.println("---- VM END ----");
    }

    /**
     * Test of getVM method, of class ActionRequester.
     */
    @Test
    public void testGetVM() {
        System.out.println("getVM");
        String application = "threeTierWebApp";
        String deployment = "100";
        String vmID = "1686";
        VM result = new ActionRequester().getVM(application, deployment, vmID);
        assert (result != null);
        printVM(result);
    }

    /**
     * Test of getVmTypesAvailableToAdd method, of class ActionRequester.
     */
    @Test
    public void testGetVmTypesAvailableToAdd() {
        System.out.println("getVmTypesAvailableToAdd");
        String applicationId = "davidgpTestApp";
        String deploymentId = "453";
        ActionRequester instance = new ActionRequester();
        List<String> result = instance.getVmTypesAvailableToAdd(applicationId, deploymentId);
        System.out.println("Types Available to Add");
        for (String type : result) {
            System.out.println(type);
        }
        System.out.println("Result Size" + result.size());
    }

    /**
     * Test of getVmTypesAvailableToRemove method, of class ActionRequester.
     */
    @Test
    public void testGetVmTypesAvailableToRemove() {
        System.out.println("getVmTypesAvailableToRemove");
        String applicationId = "threeTierWebApp";
        String deploymentId = "100";
        ActionRequester instance = new ActionRequester();
        List<String> result = instance.getVmTypesAvailableToRemove(applicationId, deploymentId);
        System.out.println("Types Available to Add");
        for (String type : result) {
            System.out.println(type);
        }
    }

    /**
     * Test of getVmIdsAvailableToRemove method, of class ActionRequester.
     */
    @Test
    public void testGetVmIdsAvailableToRemove() {
        System.out.println("getVmIdsAvailableToRemove");
        String applicationId = "davidgpTestApp";
        String deploymentId = "453";
        ActionRequester instance = new ActionRequester();
        List<Integer> result = instance.getVmIdsAvailableToRemove(applicationId, deploymentId);
    }

    /**
     * Test of getVMsOfGivenType method, of class ActionRequester.
     */
    @Test
    public void testGetVMsOfGivenType() {
        System.out.println("getVMsOfGivenType");

        String type = "mysqlA";
        ActionRequester instance = new ActionRequester();
        List<VM> vms = instance.getVMs("davidgpTestApp", "453");
        int expResult = 1;
        int result = instance.getVmCountOfGivenType(vms, type);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVMs method, of class ActionRequester.
     */
    @Test
    public void testGetVMs() {
        System.out.println("getVMs");
        String applicationId = "davidgpTestApp";
        String deploymentId = "453";
        ActionRequester instance = new ActionRequester();
        List<VM> result = instance.getVMs(applicationId, deploymentId);
        for (VM vm : result) {
            assert (vm != null);
            printVM(vm);
        }
    }
    
    /**
     * Test of getSlots method, of class ActionRequester.
     */
    @Test
    public void testGetSlots() {
        System.out.println("getSlots");
        ActionRequester instance = new ActionRequester();
        List<Slot> result = instance.getSlots();
        for (Slot slot : result) {
            System.out.println(slot.getHostname() + " - " + slot.getFreeCpus());
            //assert (vm != null);
            //printVM(vm);
        }
    }

}
