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
        for (Host host : expResult) {
            assert (result.contains(host));
        }
        assert (expResult.size() == result.size());
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
            assert (!hostMeasurement.getItems().isEmpty());
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
        assert (!result.getItems().isEmpty());
    }

    /**
     * Test of getHostData method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetHostData_List() {
        System.out.println("getHostData");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<HostMeasurement> expResult = null;
        List<HostMeasurement> result = instance.getHostData(hostList);
        assert (result != null);
        for (HostMeasurement hostMeasurement : result) {
            assert (!hostMeasurement.getItems().isEmpty());
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
    }

    /**
     * Test of getVMList method, of class ZabbixDataSourceAdaptor.
     */
    @Test
    public void testGetVMList() {
        System.out.println("getVMList");
        ZabbixDataSourceAdaptor instance = new ZabbixDataSourceAdaptor();
        List<VmDeployed> result = instance.getVmList();
        assert(!result.isEmpty());
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
        assert(result != null);
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

}
