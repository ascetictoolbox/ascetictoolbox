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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.calibration.Calibrator;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.VmMeasurement;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostVmLoadFraction;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class DataGathererTest {

    public DataGathererTest() {
    }

    private final Host CHOSEN_HOST = new Host(10084, "asok10");
    private final Host CHOSEN_HOST2 = new Host(10106, "asok12");
    private final String VM_NAME = "cloudsuite---data-analytics"; //CloudSuite - Data Analytics

    /**
     * Test of run method, of class DataGatherer.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        Thread thread = new Thread(instance);
        thread.start();
        try {
            Thread.sleep(60000 * 5); //gather 5 minutes worth of data.
        } catch (InterruptedException ex) {
            fail("Something interuppted the thread while waiting for some data to be gathered");
        }
    }

    /**
     * Test of populateHostList method, of class DataGatherer.
     */
    @Test
    public void testPopulateHostList() {
        System.out.println("populateHostList");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        instance.populateHostList();
    }

    /**
     * Test of stop method, of class DataGatherer.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        instance.stop();
    }

    /**
     * Test of getHostList method, of class DataGatherer.
     */
    @Test
    public void testGetHostList() {
        System.out.println("getHostList");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        HashMap<String, Host> result = instance.getHostList();
        assert (!result.isEmpty());
    }

    /**
     * Test of getHost method, of class DataGatherer.
     */
    @Test
    public void testGetHost() {
        System.out.println("getHost");
        String hostname = "asok10";
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        Host expResult = CHOSEN_HOST;
        Host result = instance.getHost(hostname);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVmList method, of class DataGatherer.
     */
    @Test
    public void testGetVmList() {
        System.out.println("getVmList");
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        HashMap<String, VmDeployed> result = instance.getVmList();
        assert (!result.isEmpty());
    }

    /**
     * Test of getVm method, of class DataGatherer.
     */
    @Test
    public void testGetVm() {
        System.out.println("getVm");
        String name = VM_NAME;
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        VmDeployed result = instance.getVm(name);
        assert (result != null);
        assertEquals(VM_NAME, result.getName());
    }

    /**
     * Test of getVMsOnHost method, of class DataGatherer.
     */
    @Test
    public void testGetVMsOnHost() {
        System.out.println("getVMsOnHost");
        Host host = CHOSEN_HOST2;
        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(adaptor);
        DataGatherer instance = new DataGatherer(adaptor, connector, calibrator);
        ArrayList<VmDeployed> result = instance.getVMsOnHost(host);
        assertEquals(VM_NAME, result.get(0).getName());
    }

    @Test
    public void customTest() {
        System.out.println("Custom Test - Code Patch - Line 116- 121");
        Host host = CHOSEN_HOST2;
        ZabbixDataSourceAdaptor datasource = new ZabbixDataSourceAdaptor();
        DefaultDatabaseConnector connector = new DefaultDatabaseConnector();
        Calibrator calibrator = new Calibrator(datasource);
        DataGatherer instance = new DataGatherer(datasource, connector, calibrator);
        ArrayList<VmDeployed> vms = instance.getVMsOnHost(host);
        if (!vms.isEmpty()) {
            HostVmLoadFraction fraction = new HostVmLoadFraction(host, (new GregorianCalendar().getTimeInMillis()) / 1000);
            List<VmMeasurement> vmMeasurements = datasource.getVmData(vms);
            fraction.setFraction(vmMeasurements);
            for (VmMeasurement vmMeasurement : vmMeasurements) {
                System.out.println("VM: " + vmMeasurement.getVm().getName());
                System.out.println("Fraction: " + fraction.getFraction(vmMeasurement.getVm()));
            }
        } else
        {
            fail("The VM was not found");
        }
    }

}
