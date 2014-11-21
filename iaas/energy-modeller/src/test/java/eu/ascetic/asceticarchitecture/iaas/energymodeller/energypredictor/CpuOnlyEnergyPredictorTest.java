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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Richard
 */
public class CpuOnlyEnergyPredictorTest {

    public CpuOnlyEnergyPredictorTest() {

    }

    public Host host = new Host(10105, "asok12");
    public VM vm1 = new VM(2, 1548, 128);
    public VM vm2 = new VM(4, 1524, 256);
    public Collection<VM> vms = new ArrayList<>();

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

    public void addVMs(VM vm) {
        vms.add(vm);
    }

    @Test
    public void TestGetHostPredictedEnergy() {
        System.out.println("getHostPredictedEnergy");
        EnergyUsagePrediction prediction;

        addVMs(vm1);
        addVMs(vm2);

        CpuOnlyEnergyPredictor predictor = new CpuOnlyEnergyPredictor();
        setCalibrationData(host);
        host.setRamMb(32244);
        prediction = predictor.getHostPredictedEnergy(host, vms);
        System.out.println("Host: " + host.getHostName());
        System.out.println("VM Count: " + vms.size());
        System.out.println("store values size is: " + host.getCalibrationData().size());
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }

    private Host setCalibrationData(Host host) {
        DefaultDatabaseConnector db = new DefaultDatabaseConnector();
        return db.getHostCalibrationData(host);
    }

    @Test
    public void TestGetVMPredictedEnergy() {
        System.out.println("getVMPredictedEnergy");
        EnergyUsagePrediction prediction;
        addVMs(vm1);
        addVMs(vm2);

        System.out.println("store values size is: " + host.getCalibrationData().size());

        CpuOnlyEnergyPredictor predictor = new CpuOnlyEnergyPredictor();
        setCalibrationData(host);
        host.setRamMb(32244);
        System.out.println("VM for Energy Prediction: " + vm1.toString());
        System.out.println("Amount of VMs Inducing Load: " + vms.size());
        System.out.println("Host To Query: " + host.getHostName());
        prediction = predictor.getVMPredictedEnergy(vm1, vms, host);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }

    @Test
    public void TestRetrieveModel() {
        CpuOnlyEnergyPredictor predictor = new CpuOnlyEnergyPredictor();
        setCalibrationData(host);
        EnergyModel model = predictor.retrieveModel(host);
        System.out.println("Y = " + model.getCoefCPU() + " X + " + model.getIntercept());
        assert model != null;
        assert model.getIntercept() > 0;
    }    
    
}
