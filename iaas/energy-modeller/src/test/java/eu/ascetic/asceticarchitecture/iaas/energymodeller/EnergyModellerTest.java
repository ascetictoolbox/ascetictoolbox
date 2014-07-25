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
package eu.ascetic.asceticarchitecture.iaas.energymodeller;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class EnergyModellerTest {

    public EnergyModellerTest() {
    }

    private final String VM_NAME = "cloudsuite---data-analytics"; //CloudSuite - Data Analytics
    private final String HOST_NAME = "asok10";
    private final String HOST_NAME_WITH_VM = "asok12";
    EnergyModeller instance = new EnergyModeller();

    /**
     * Test of setEnergyPredictor method, of class EnergyModeller.
     */
    @Test
    public void testSetEnergyPredictor() {
        System.out.println("setEnergyPredictor");
        String energyPredictor = "DefaultEnergyPredictor";
        EnergyModeller instance2 = new EnergyModeller();
        instance2.setEnergyPredictor(energyPredictor);
    }

    /**
     * Test of setDataSource method, of class EnergyModeller.
     */
    @Test
    public void testSetDataSource() {
        System.out.println("setDataSource");
        String dataSource = "ZabbixDataSourceAdaptor";
        EnergyModeller instance2 = new EnergyModeller();
        instance2.setDataSource(dataSource);
    }

    /**
     * Test of getEnergyRecordForVM method, of class EnergyModeller.
     */
    @Test
    public void testGetEnergyRecordForVM_Collection_TimePeriod() {
        System.out.println("getEnergyRecordForVM");
        Collection<VmDeployed> vms = new ArrayList<>();
        TimePeriod timePeriod = null;
        VmDeployed vm = instance.getVM(VM_NAME);
        vms.add(vm);
        HashSet<HistoricUsageRecord> result = instance.getEnergyRecordForVM(vms, timePeriod);
        assert (result != null);
        assert (result.size() == 1);
        for (HistoricUsageRecord usageRecord : result) {
            System.out.println("Usage for: " + vm.getName());
            System.out.println("Average Power: " + usageRecord.getAvgPowerUsed());
            System.out.println("Total Energy used: " + usageRecord.getTotalEnergyUsed());
            System.out.println("Time in Seconds (Duration): " + usageRecord.getDuration().getDuration());
            assert (usageRecord.getEnergyUser().contains(vm));
            assert (usageRecord.getEnergyUser().size() == 1);
            assert (usageRecord.getAvgPowerUsed() > 0);
            assert (usageRecord.getAvgPowerUsed() < 500);
            assert (usageRecord.getTotalEnergyUsed() > 0);
        }
    }

    /**
     * Test of getCurrentEnergyForVM method, of class EnergyModeller.
     */
    @Test
    public void testGetCurrentEnergyForVM_Collection() {
        System.out.println("getCurrentEnergyForVM");
        Collection<VmDeployed> vms = new ArrayList<>();
        VmDeployed vm = instance.getVM(VM_NAME);
        vms.add(vm);
        HashSet<CurrentUsageRecord> result = instance.getCurrentEnergyForVM(vms);
        assert (result != null);
        assert (result.size() == 1);
        for (CurrentUsageRecord usageRecord : result) {
            assert (usageRecord.getPower() > 200);
            assert (usageRecord.getPower() < 500);
            System.out.println("Usage for: " + vm.getName());
            System.out.println("Power: " + usageRecord.getPower());
        }
    }

    /**
     * Test of getEnergyRecordForHost method, of class EnergyModeller.
     */
    @Test
    public void testGetEnergyRecordForHost_Collection_TimePeriod() {
        System.out.println("getEnergyRecordForHost");
        Collection<Host> hosts = new HashSet<>();
        TimePeriod timePeriod = null;
        Host host = instance.getHost(HOST_NAME);
        hosts.add(host);
        HashSet<HistoricUsageRecord> result = instance.getEnergyRecordForHost(hosts, timePeriod);
        assert (result != null);
        assert (result.size() == 1);
        for (HistoricUsageRecord usageRecord : result) {
            System.out.println("Usage for: " + host.getHostName());
            System.out.println("Average Power: " + usageRecord.getAvgPowerUsed());
            System.out.println("Total Energy used: " + usageRecord.getTotalEnergyUsed());
            System.out.println("Time in Seconds (Duration): " + usageRecord.getDuration().getDuration());
            assert (usageRecord.getEnergyUser().contains(host));
            assert (usageRecord.getEnergyUser().size() == 1);
            assert (usageRecord.getAvgPowerUsed() > 0);
            assert (usageRecord.getAvgPowerUsed() < 500);
            assert (usageRecord.getTotalEnergyUsed() > 0);
        }
    }

    /**
     * Test of getCurrentEnergyForHost method, of class EnergyModeller.
     */
    @Test
    public void testGetCurrentEnergyForHost_Collection() {
        System.out.println("getCurrentEnergyForHost");
        Collection<Host> hosts = new ArrayList<>();
        Host host1 = instance.getHost(HOST_NAME);
        Host host2 = instance.getHost(HOST_NAME_WITH_VM);
        hosts.add(host1);
        hosts.add(host2);
        HashSet<CurrentUsageRecord> result = instance.getCurrentEnergyForHost(hosts);
        assert (result != null);
        assert (result.size() == 2);
        for (CurrentUsageRecord usageRecord : result) {
            assert (usageRecord.containsEnergyUser(host1) || usageRecord.containsEnergyUser(host2));
            assert (usageRecord.getPower() > 200);
            assert (usageRecord.getPower() < 500);
            if (usageRecord.getEnergyUser().contains(host1)) {
                System.out.println("Usage for: " + host1.getHostName());
            }
            if (usageRecord.getEnergyUser().contains(host2)) {
                System.out.println("Usage for: " + host2.getHostName());
            }
            System.out.println("Power: " + usageRecord.getPower());
        }
    }

    /**
     * Test of getCurrentEnergyForVM method, of class EnergyModeller.
     */
    @Test
    public void testGetCurrentEnergyForVM_VmDeployed() {
        System.out.println("getCurrentEnergyForVM");
        VmDeployed vm;
        vm = instance.getVM(VM_NAME);
        CurrentUsageRecord result = instance.getCurrentEnergyForVM(vm);
        assert (result != null);
        assert (result.getEnergyUser().contains(vm));
        assert (result.getEnergyUser().size() == 1);
        assert (result.getPower() > 0);
        assert (result.getPower() < 500);
        System.out.println("Usage for: " + vm.getName());
        System.out.println("Power: " + result.getPower());
    }

    /**
     * Test of getEnergyRecordForVM method, of class EnergyModeller.
     */
    @Test
    public void testGetEnergyRecordForVM_VmDeployed_TimePeriod() {
        System.out.println("getEnergyRecordForVM");
        TimePeriod timePeriod = null;
        VmDeployed vm = instance.getVM(VM_NAME);
        HistoricUsageRecord result = instance.getEnergyRecordForVM(vm, timePeriod);
        System.out.println("Usage for: " + vm.getName());
        System.out.println("Average Power: " + result.getAvgPowerUsed());
        System.out.println("Total Energy used: " + result.getTotalEnergyUsed());
        System.out.println("Time in Seconds (Duration): " + result.getDuration().getDuration());
        assert (result != null);
        assert (result.getEnergyUser().contains(vm));
        assert (result.getEnergyUser().size() == 1);
        assert (result.getAvgPowerUsed() > 0);
        assert (result.getAvgPowerUsed() < 500);
        assert (result.getTotalEnergyUsed() > 0);
    }

    /**
     * Test of getEnergyRecordForHost method, of class EnergyModeller.
     */
    @Test
    public void testGetEnergyRecordForHost_Host_TimePeriod() {
        System.out.println("getEnergyRecordForHost");
        TimePeriod timePeriod = null;
        Host host = instance.getHost(HOST_NAME);
        HistoricUsageRecord result = instance.getEnergyRecordForHost(host, timePeriod);
        System.out.println("Usage for: " + host.getHostName());
        System.out.println("Average Power: " + result.getAvgPowerUsed());
        System.out.println("Total Energy used: " + result.getTotalEnergyUsed());
        System.out.println("Time in Seconds (Duration): " + result.getDuration().getDuration());
        assert (result != null);
        assert (result.getEnergyUser().contains(host));
        assert (result.getEnergyUser().size() == 1);
        assert (result.getAvgPowerUsed() > 0);
        assert (result.getAvgPowerUsed() < 500);
        assert (result.getTotalEnergyUsed() > 0);
    }

    /**
     * Test of getCurrentEnergyForHost method, of class EnergyModeller.
     */
    @Test
    public void testGetCurrentEnergyForHost_Host() {
        System.out.println("getCurrentEnergyForHost");
        Host host = instance.getHost(HOST_NAME);
        CurrentUsageRecord result = instance.getCurrentEnergyForHost(host);
        assert (result != null);
        assert (result.getEnergyUser().contains(host));
        assert (result.getEnergyUser().size() == 1);
        assert (result.getPower() > 0);
        assert (result.getPower() < 500);
        System.out.println("Usage for: " + host.getHostName());
        System.out.println("Average Power: " + result.getPower());
    }

    /**
     * Test of getPredictedEnergyForVM method, of class EnergyModeller.
     */
    @Test
    public void testGetPredictedEnergyForVM() {
        System.out.println("getPredictedEnergyForVM");
        VM vmImage;
        Collection<VM> vMsOnHost;
        Host host;
        vmImage = new VM(1, 512, 20);
        host = instance.getHost(HOST_NAME_WITH_VM);
        vMsOnHost = new HashSet<>();
        vMsOnHost.add(instance.getVM(VM_NAME));
        EnergyUsagePrediction result = instance.getPredictedEnergyForVM(vmImage, vMsOnHost, host);
        assert (result != null);
        assert (result.getEnergyUser().contains(vmImage));
        assert (result.getEnergyUser().size() == 1);
        System.out.println("Predicted Usage On Host: " + host.getHostName());
        System.out.println("Predicted Usage For VM: " + vmImage);
        System.out.println("Average Power: " + result.getAvgPowerUsed());
        System.out.println("Total Energy used: " + result.getTotalEnergyUsed());
        assert (result.getAvgPowerUsed() > 0);
        assert (result.getAvgPowerUsed() < 500);
        assert (result.getTotalEnergyUsed() > 0);
    }

    /**
     * Test of getHostPredictedEnergy method, of class EnergyModeller.
     */
    @Test
    public void testGetHostPredictedEnergy() {
        System.out.println("getHostPredictedEnergy");
        Host host;
        Collection<VM> virtualMachines = new ArrayList<>();
        host = instance.getHost(HOST_NAME);
        EnergyUsagePrediction result = instance.getHostPredictedEnergy(host, virtualMachines);
        assert (result != null);
        assert (result.getEnergyUser().contains(host));
        System.out.println("Predicted Usage On Host: " + host.getHostName());
        System.out.println("Average Power: " + result.getAvgPowerUsed());
        System.out.println("Total Energy used: " + result.getTotalEnergyUsed());
        assert (result.getEnergyUser().size() == 1);
        assert (result.getAvgPowerUsed() > 0);
        assert (result.getAvgPowerUsed() < 500);
        assert (result.getTotalEnergyUsed() > 0);   
    }

    /**
     * Test of getHost method, of class EnergyModeller.
     */
    @Test
    public void testGetHost_Collection() {
        System.out.println("getHost");
        Collection<String> hostname = new HashSet<>();
        hostname.add(HOST_NAME);
        Collection<Host> result = instance.getHost(hostname);
        assert (result != null);
        for (Host host : result) {
            assertEquals(host.getHostName(), HOST_NAME);
            System.out.println("Host Name: " + host.getHostName());

        }
    }

    /**
     * Test of getHost method, of class EnergyModeller.
     */
    @Test
    public void testGetHost_String() {
        System.out.println("getHost");
        String hostname = HOST_NAME;
        Host result = instance.getHost(hostname);
        assert (result != null);
        assert (result.getHostName().equals(HOST_NAME));
        System.out.println("Host Name: " + result.getHostName());
    }

    /**
     * Test of getVM method, of class EnergyModeller.
     */
    @Test
    public void testGetVM_3args() {
        System.out.println("getVM");
        int cpuCount = 0;
        int ramMb = 0;
        int diskGb = 0;
        VM expResult = new VM(ramMb, ramMb, diskGb);
        VM result = EnergyModeller.getVM(cpuCount, ramMb, diskGb);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVM method, of class EnergyModeller.
     */
    @Test
    public void testGetVM_String() {
        System.out.println("getVM");
        String name = VM_NAME;
        VmDeployed result = instance.getVM(name);
        assert (result != null);
        assert (result.getName().equals(VM_NAME));
        System.out.println("VM Name: " + result.getName());
    }

    /**
     * Test of calibrateModelForHost method, of class EnergyModeller.
     */
    @Test
    public void testCalibrateModelForHost_Collection() {
        System.out.println("calibrateModelForHost");
        Collection<Host> hosts = new HashSet<>();
        hosts.add(instance.getHost(HOST_NAME));
        instance.calibrateModelForHost(hosts);
    }

    /**
     * Test of calibrateModelForHost method, of class EnergyModeller.
     */
    @Test
    public void testCalibrateModelForHost_Host() {
        System.out.println("calibrateModelForHost");
        Host host;
        host = instance.getHost(HOST_NAME);
        if (!host.isCalibrated()) {
            instance.calibrateModelForHost(host);
        }
    }

}
