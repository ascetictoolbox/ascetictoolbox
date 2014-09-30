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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.Configuration;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter.NameBeginsFilter;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter.ZabbixHostVMFilter;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class ZabbixDataSourceAdaptorTest {

    private final List<Host> hostList = new ArrayList<>();
    private final Host CHOSEN_HOST = new Host(10084, "asok10");
    private final String VM_NAME = "cloudsuite---data-analytics"; //CloudSuite - Data Analytics

    public ZabbixDataSourceAdaptorTest() {
        try {
            /**
             * This will save the configuration file to disk. In case the
             * defaults need setting. It will prevent this test code failing in
             * the event of clean and build or running from a fresh
             * installation.
             */
            org.apache.commons.configuration.PropertiesConfiguration config = new PropertiesConfiguration("ascetic-zabbix-api.properties");
            config.getString("zabbix.server.url", "http://10.4.0.15/zabbix/api_jsonrpc.php");
            config.getString("zabbix.user", "admin");
            config.getString("zabbix.password", "73046447cce977b10167");
            config.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }

        hostList.add(new Host(10105, "asok09"));
        hostList.add(new Host(10084, "asok10"));
        hostList.add(new Host(10107, "asok11"));
        hostList.add(new Host(10106, "asok12"));

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
     * Test of main method, of class ZabbixDataSourceAdaptor.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        ZabbixDataSourceAdaptor.main(args);
    }

    /**
     * Test of getHostByName method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostByName() {
        System.out.println("getHostByName");
        String hostname = "asok10";
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        Host expResult = CHOSEN_HOST;
        Host result = instance.getHostByName(hostname);
        assertEquals(expResult, result);
    }

    /**
     * Test of getHostList method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostList() {
        System.out.println("getHostList");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<Host> expResult = new ArrayList<>();
        expResult.addAll(hostList);
        List<Host> result = instance.getHostList();
        assertEquals(result.size(), 4);
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
     * Test of getHostData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_0args() {
        System.out.println("getHostData");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<HostMeasurement> result = instance.getHostData();
        assert (result != null);
        for (HostMeasurement hostMeasurement : result) {
            assert (hostMeasurement.getMetricCount() != 0);
        }
    }

    /**
     * Test of getHostData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_Host() {
        System.out.println("getHostData");
        Host host = CHOSEN_HOST;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        HostMeasurement result = instance.getHostData(host);
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
     * Test of getHostData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_List() {
        System.out.println("getHostData");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<HostMeasurement> result = instance.getHostData(hostList);
        assert (result != null);
        for (HostMeasurement hostMeasurement : result) {
            assert (hostMeasurement.getMetricCount() != 0);
        }
    }

    /**
     * Test of getClient method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetClient() {
        System.out.println("getClient");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        ZabbixClient result = instance.getClient();
        assert (result != null);
    }

    /**
     * Test of setClient method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testSetClient() {
        System.out.println("setClient");
        ZabbixClient client = new ZabbixClient();
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        instance.setClient(client);
        assertEquals(client, instance.getClient());
    }

    /**
     * Test of getCurrentEnergyUsage method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetCurrentEnergyUsage() {
        System.out.println("getCurrentEnergyUsage");
        Host host = CHOSEN_HOST;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        CurrentUsageRecord result = instance.getCurrentEnergyUsage(host);
        assert (result != null);
        assert (result.getPower() > 0.0);
        System.out.println("Host Name: " + host.getHostName());
        System.out.println("Host Id: " + host.getId());
        System.out.println("Host Current Power Usage: " + result.getPower());
    }

    /**
     * Test of getLowestHostPowerUsage method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetLowestHostPowerUsage() {
        System.out.println("getLowestHostPowerUsage");
        Host host = CHOSEN_HOST;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        double result = instance.getLowestHostPowerUsage(host);
        assert (result > 0.0);
        System.out.println("Host Name: " + host.getHostName());
        System.out.println("Host Id: " + host.getId());
        System.out.println("Lowest Power: " + result);
    }

    /**
     * Test of getVMList method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVMList() {
        System.out.println("getVMList");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<VmDeployed> result = instance.getVmList();
        assert (!result.isEmpty());
        for (VmDeployed vmDeployed : result) {
            System.out.println("Name: " + vmDeployed.getName());
            System.out.println("Id: " + vmDeployed.getId());
            System.out.println("Created: " + vmDeployed.getCreated());
            System.out.println("CPU Count: " + vmDeployed.getCpus());
            System.out.println("Disks GB: " + vmDeployed.getDiskGb());
            System.out.println("Memory Mb: " + vmDeployed.getRamMb());
            System.out.println("State: " + vmDeployed.getState());
        }
    }

    /**
     * Test of getHostFilter method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostFilter() {
        System.out.println("getHostFilter");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        ZabbixHostVMFilter result = instance.getHostFilter();
        assert (result != null);
    }

    /**
     * Test of setHostFilter method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testSetHostFilter() {
        System.out.println("setHostFilter");
        ZabbixHostVMFilter hostFilter = new NameBeginsFilter();
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        instance.setHostFilter(hostFilter);
    }

    /**
     * Test of getVmList method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVmList() {
        System.out.println("getVmList");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<VmDeployed> result = instance.getVmList();
        assert (result != null);
        assert (!result.isEmpty());
        for (VmDeployed vmDeployed : result) {
            assert (vmDeployed.getName() != null);
            assert (vmDeployed.getId() > 0);
            System.out.println("VM name: " + vmDeployed.getName());
            System.out.println("VM id: " + vmDeployed.getId());
            System.out.println("VM Ram: " + vmDeployed.getRamMb());
            System.out.println("VM Disk: " + vmDeployed.getDiskGb());
        }
    }

    /**
     * Test of getVmData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_0args() {
        System.out.println("getVmData");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<VmMeasurement> result = instance.getVmData();
        assert (result != null);
        assert (!result.isEmpty());
    }

    /**
     * Test of getVmData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_VmDeployed() {
        System.out.println("getVmData");
        VmDeployed vm;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
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
     * Test of getVmData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVmData_List() {
        System.out.println("getVmData");
        List<VmDeployed> vmList = new ArrayList<>();
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        vmList.add(instance.getVmByName(VM_NAME));
        List<VmMeasurement> result = instance.getVmData(vmList);
        assert (result != null);
        for (VmMeasurement vmMeasurement : result) {
            System.out.println("Name: " + vmMeasurement.getVm().getName());
        }

    }

    /**
     * Test of getVmByName method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVmByName() {
        System.out.println("getVmByName");
        String name = VM_NAME;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
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
     * Test of getHighestHostPowerUsage method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHighestHostPowerUsage() {
        System.out.println("getHighestHostPowerUsage");
        Host host = CHOSEN_HOST;
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        double result = instance.getHighestHostPowerUsage(host);
        assert(result > 0.0);
        System.out.println("Highest Host Power Usage: " + result);
    }

    /**
     * Test of getCpuUtilisation method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetCpuUtilisation() {
        System.out.println("getCpuUtilisation");
        Host host = CHOSEN_HOST;
        int duration = (int) TimeUnit.MINUTES.toSeconds(2); //time unit in seconds so run for 2 minutes.
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        double result = instance.getCpuUtilisation(host, duration);
        assert(result >= 0);
        assert(result <= 1.0);
        System.out.println("CPU Utilisation Last " + duration + " minutes: " + result);
        
    }

}
