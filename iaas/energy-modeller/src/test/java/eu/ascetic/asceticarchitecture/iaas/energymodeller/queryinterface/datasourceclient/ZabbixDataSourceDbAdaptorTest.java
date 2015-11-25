/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is the test class for the data source adaptor that connects directly
 * into the Zabbix Database for its information.
 *
 * @author Richard Kavanagh
 */
public class ZabbixDataSourceDbAdaptorTest {

    private final List<Host> hostList = new ArrayList<>();
    private final Host CHOSEN_HOST = new Host(10140, "Wally152");
    private final String VM_NAME = "IaaS_VM_Dev"; //CloudSuite - Data Analytics
    private final ZabbixDirectDbDataSourceAdaptor instance = new ZabbixDirectDbDataSourceAdaptor();

    public ZabbixDataSourceDbAdaptorTest() {

        hostList.add(new Host(10107, "wally198"));
        hostList.add(new Host(10108, "wally197"));
        hostList.add(new Host(10109, "wally196"));
        hostList.add(new Host(10110, "wally195"));
        hostList.add(new Host(10111, "wally193"));
        hostList.add(new Host(10112, "wally157"));
        hostList.add(new Host(10113, "wally158"));
        hostList.add(new Host(10114, "wally159"));
        hostList.add(new Host(10115, "wally160"));
        hostList.add(new Host(10116, "wally161"));
        hostList.add(new Host(10117, "wally162"));
        hostList.add(new Host(10118, "wally163"));
        hostList.add(new Host(10119, "wally164"));
        hostList.add(new Host(10120, "wally165"));
        hostList.add(new Host(10121, "wally166"));
        hostList.add(new Host(10122, "wally167"));
        hostList.add(new Host(10123, "wally168"));
        hostList.add(new Host(10124, "wally169"));
        hostList.add(new Host(10125, "wally170"));
        hostList.add(new Host(10126, "wally171"));
        hostList.add(new Host(10127, "wally172"));
        hostList.add(new Host(10128, "wally173"));
        hostList.add(new Host(10129, "wally174"));
        hostList.add(new Host(10130, "wally175"));
        hostList.add(new Host(10131, "wally176"));
        hostList.add(new Host(10132, "wally177"));
        hostList.add(new Host(10133, "wally178"));
        hostList.add(new Host(10134, "wally179"));
        hostList.add(new Host(10135, "wally180"));
        hostList.add(new Host(10136, "wally181"));
        hostList.add(new Host(10137, "wally182"));
        hostList.add(new Host(10140, "wally152"));
        hostList.add(new Host(10141, "wally153"));
        hostList.add(new Host(10142, "wally154"));
        hostList.add(new Host(10143, "wally155"));
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getHostByName method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostByName() {
        System.out.println("getHostByName");
        String hostname = "Wally152";
        Host expResult = CHOSEN_HOST;
        Host result = instance.getHostByName(hostname);
        assertEquals(expResult, result);
    }

    /**
     * Test of getHostList method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostList() {
        System.out.println("getHostList");
        List<Host> expResult = new ArrayList<>();
        expResult.addAll(hostList);
        List<Host> result = instance.getHostList();
        assertEquals(result.size(), 31);
        for (Host host : expResult) {
            assert (result.contains(host));
            System.out.println("Host name: " + host.getHostName());
            System.out.println("Host id: " + host.getId());
        }
        for (Host host : result) {
            assert (host.getRamMb() > 0);
            assert (host.getDiskGb() > 0);
            System.out.println("Host ram: " + host.getRamMb());
            System.out.println("Host disk: " + host.getDiskGb());
        }
        assertEquals(expResult.size(), result.size());
    }

    /**
     * Test of getHostList method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostListNamedGroup() {
        System.out.println("getHostList");
        List<Host> expResult = new ArrayList<>();
        expResult.addAll(hostList);
        List<Host> result = instance.getHostList("Hypervisors"); //Virtual Machines
        assertEquals(result.size(), 31);
        for (Host host : expResult) {
            assert (result.contains(host));
            System.out.println("Host name: " + host.getHostName());
            System.out.println("Host id: " + host.getId());
        }
        for (Host host : result) {
            assert (host.getRamMb() >= 0);
            assert (host.getDiskGb() >= 0);
            System.out.println("Host: " + host.getHostName());
            System.out.println("Host ram: " + host.getRamMb());
            System.out.println("Host disk: " + host.getDiskGb());
        }
//        assertEquals(expResult.size(), result.size());
    }

    /**
     * Test of getHostAndVmList method, of class
     * ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostAndVmList() {
        System.out.println("getHostAndVmList");
        List<EnergyUsageSource> result = instance.getHostAndVmList();
        for (EnergyUsageSource source : result) {
            if (source instanceof Host) {
                Host host = (Host) source;
                System.out.println("Host name: " + host.getHostName());
                System.out.println("Host id: " + host.getId());
            } else {
                VmDeployed vm = (VmDeployed) source;
                System.out.println("VM Name: " + vm.getName());
                System.out.println("VM Id: " + vm.getId());
            }
        }
    }

    /**
     * Test of getHostData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_0args() {
        System.out.println("getHostData");
        List<HostMeasurement> result = instance.getHostData();
        assert (result != null);
        for (HostMeasurement hostMeasurement : result) {
            assert (hostMeasurement.getMetricCount() != 0);
        }
    }

    /**
     * Test of getHostData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_Host() {
        System.out.println("getHostData");
        Host host = CHOSEN_HOST;
        long one = System.currentTimeMillis();
        HostMeasurement result = instance.getHostData(host);
        long two = System.currentTimeMillis();
        instance.getHostData(host);
        long three = System.currentTimeMillis();
        instance.getHostData(host);
        long four = System.currentTimeMillis();
        instance.getHostData(host);
        long five = System.currentTimeMillis();
        result = instance.getHostData(host);
        long six = System.currentTimeMillis();
        instance.getHostData(host);
        long seven = System.currentTimeMillis();
        instance.getHostData(host);
        long eight = System.currentTimeMillis();
        instance.getHostData(host);
        long nine = System.currentTimeMillis();
        System.out.println("First Call: " + (two - one));
        System.out.println("Second Call: " + (three - two));
        System.out.println("Third Call: " + (four - three));
        System.out.println("Fourth Call: " + (five - four));
        System.out.println("Fifth Call: " + (six - five));
        System.out.println("Sixth Call: " + (seven - six));
        System.out.println("Seventh Call: " + (eight - seven));
        System.out.println("Eighth Call: " + (nine - eight));
        assert (result != null);
        assert (result.getMetricCount() != 0);
        System.out.println("Clock: " + result.getClock());
        System.out.println("Energy: " + result.getEnergy());
        System.out.println("Power: " + result.getPower());
        System.out.println("Memory Total: " + result.getMemoryTotal());
        System.out.println("Memory Used: " + result.getMemoryUsed());
        System.out.println("Memory Available: " + result.getMemoryAvailable());
        System.out.println("CPU Load: " + result.getCpuUtilisation());
        System.out.println("CPU Idle: " + result.getCpuIdle());
        System.out.println("Network In: " + result.getNetworkIn());
        System.out.println("Network Out: " + result.getNetworkOut());

    }

    /**
     * Test of getHostData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_List() {
        System.out.println("getHostData");
        List<HostMeasurement> result = instance.getHostData(hostList);
        assert (result != null);
        for (HostMeasurement hostMeasurement : result) {
            assert (hostMeasurement.getMetricCount() != 0);
        }
    }

    /**
     * Test of getCurrentEnergyUsage method, of class
     * ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetCurrentEnergyUsage() {
        System.out.println("getCurrentEnergyUsage");
        Host host = CHOSEN_HOST;
        CurrentUsageRecord result = instance.getCurrentEnergyUsage(host);
        assert (result != null);
        assert (result.getPower() > 0.0);
        System.out.println("Host Name: " + host.getHostName());
        System.out.println("Host Id: " + host.getId());
        System.out.println("Host Current Power Usage: " + result.getPower());
    }

    /**
     * Test of getLowestHostPowerUsage method, of class
     * ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetLowestHostPowerUsage() {
        System.out.println("getLowestHostPowerUsage");
        Host host = CHOSEN_HOST;
        double result = instance.getLowestHostPowerUsage(host);
        assert (result > 0.0);
        System.out.println("Host Name: " + host.getHostName());
        System.out.println("Host Id: " + host.getId());
        System.out.println("Lowest Power: " + result);
    }

    /**
     * Test of getVMList method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetVmList() {
        System.out.println("getVMList");
        List<VmDeployed> result = instance.getVmList();
        assert (result != null);
        assert (!result.isEmpty());
        for (VmDeployed vmDeployed : result) {
            assert (vmDeployed.getName() != null);
            assert (vmDeployed.getId() > 0);
            System.out.println("VM Name: " + vmDeployed.getName());
            System.out.println("VM Id: " + vmDeployed.getId());
            System.out.println("Created: " + vmDeployed.getCreated());
            System.out.println("CPU Count: " + vmDeployed.getCpus());
            System.out.println("Disks GB: " + vmDeployed.getDiskGb());
            System.out.println("Memory Mb: " + vmDeployed.getRamMb());
            System.out.println("State: " + vmDeployed.getState());
            if (vmDeployed.getAllocatedTo() != null) {
                System.out.println("Allocated To: " + vmDeployed.getAllocatedTo().getHostName());
            } else {
                System.out.println("Allocated To: UNKNOWN!!");
            }
        }
    }

    /**
     * Test of getVmData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_0args() {
        System.out.println("getVmData");
        List<VmMeasurement> result = instance.getVmData();
        assert (result != null);
        assert (!result.isEmpty());
    }

    /**
     * Test of getVmData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_VmDeployed() {
        System.out.println("getVmData");
        VmDeployed vm;
        vm = instance.getVmByName(VM_NAME);
        VmMeasurement result = instance.getVmData(vm);
        assert (result != null);
        assert (result.getMetricCount() != 0);
        System.out.println("VM Metric List");
        for (String name : result.getMetricNameList()) {
            System.out.println(name);
        }
    }

    /**
     * Test of getVmData method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_List() {
        System.out.println("getVmData");
        List<VmDeployed> vmList = new ArrayList<>();
        vmList.addAll(instance.getVmList());
        List<VmMeasurement> result = instance.getVmData(vmList);
        assert (result != null);
        for (VmMeasurement vmMeasurement : result) {
            System.out.println("Name: " + vmMeasurement.getVm().getName());
            for (String vmMeasurement1 : vmMeasurement.getMetricNameList()) {
                System.out.println("---- Name: " + vmMeasurement1);
            }

        }

    }

    /**
     * Test of getVmByName method, of class ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetVmByName() {
        System.out.println("getVmByName");
        String name = VM_NAME;
        VmDeployed result = instance.getVmByName(name);
        assert (result != null);
        System.out.println("Name: " + result.getName());
        System.out.println("Id: " + result.getId());
        System.out.println("Created: " + result.getCreated());
        System.out.println("CPU Count: " + result.getCpus());
        System.out.println("Disks GB: " + result.getDiskGb());
        System.out.println("Memory Mb: " + result.getRamMb());
        System.out.println("State: " + result.getState());
    }

    /**
     * Test of getHighestHostPowerUsage method, of class
     * ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetHighestHostPowerUsage() {
        System.out.println("getHighestHostPowerUsage");
        Host host = CHOSEN_HOST;
        double result = instance.getHighestHostPowerUsage(host);
        assert (result > 0.0);
        System.out.println("Highest Host Power Usage: " + result);
    }

    /**
     * Test of getCpuUtilisation method, of class
     * ZabbixDirectDbDataSourceAdaptor.
     */
    @Test
    public void testGetCpuUtilisation() {
        System.out.println("getCpuUtilisation");
        Host host = CHOSEN_HOST;
        int duration = (int) TimeUnit.MINUTES.toSeconds(2); //time unit in seconds so run for 2 minutes.
        double result = instance.getCpuUtilisation(host, duration);
        assert (result >= 0);
        assert (result <= 1.0);
        System.out.println("CPU Utilisation Last " + duration + " minutes: " + result);
    }

}
