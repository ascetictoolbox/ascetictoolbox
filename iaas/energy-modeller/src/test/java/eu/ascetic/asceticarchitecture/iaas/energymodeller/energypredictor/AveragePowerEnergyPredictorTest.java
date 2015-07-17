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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The test class of the Average Power energy predictor
 *
 * @author Richard Kavanagh
 */
public class AveragePowerEnergyPredictorTest {

    public AveragePowerEnergyPredictorTest() {
    }

    public Host host = new Host(10115, "wally160");
    public VM vm1 = new VM(2, 1548, 128);
    public VM vm2 = new VM(4, 1524, 256);
    public Collection<VM> vms = new ArrayList<>();

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public void addVMs(VM vm) {
        vms.add(vm);
    }

    /**
     * Test of getHostPredictedEnergy method, of class
     * AveragePowerEnergyPredictor.
     */
    @Test
    public void testGetHostPredictedEnergy() {
        System.out.println("getHostPredictedEnergy");
        EnergyUsagePrediction prediction;

        addVMs(vm1);
        addVMs(vm2);

        AveragePowerEnergyPredictor predictor = new AveragePowerEnergyPredictor();
        host.setRamMb(32244);
        prediction = predictor.getHostPredictedEnergy(host, vms);
        System.out.println("Host: " + host.getHostName());
        System.out.println("VM Count: " + vms.size());
        System.out.println("store values size is: " + host.getCalibrationData().size());
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }

    /**
     * Test of getVMPredictedEnergy method, of class
     * AveragePowerEnergyPredictor.
     */
    @Test
    public void testGetVMPredictedEnergy() {
        System.out.println("getVMPredictedEnergy");
        EnergyUsagePrediction prediction;
        addVMs(vm1);
        addVMs(vm2);

        System.out.println("store values size is: " + host.getCalibrationData().size());

        AveragePowerEnergyPredictor predictor = new AveragePowerEnergyPredictor();
        host.setRamMb(32244);
        System.out.println("VM for Energy Prediction: " + vm1.toString());
        System.out.println("Amount of VMs Inducing Load: " + vms.size());
        System.out.println("Host To Query: " + host.getHostName());
        prediction = predictor.getVMPredictedEnergy(vm1, vms, host);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());
    }

    /**
     * Test of predictPowerUsed method, of class AveragePowerEnergyPredictor.
     */
    @Test
    public void testPredictPowerUsed_Host() {
        System.out.println("predictPowerUsed");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        double result = instance.predictPowerUsed(host);
        assert (result >= 0.0);
    }

    /**
     * Test of predictPowerUsed method, of class AveragePowerEnergyPredictor.
     */
    @Test
    public void testPredictPowerUsed_Host_double() {
        System.out.println("predictPowerUsed");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        double result = instance.predictPowerUsed(host, 0.0);
        assert (result >= 0.0);
    }

    /**
     * Test of getSumOfSquareError method, of class AveragePowerEnergyPredictor.
     */
    @Test
    public void testGetSumOfSquareError() {
        System.out.println("getSumOfSquareError");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        double expResult = Double.MAX_VALUE;
        double result = instance.getSumOfSquareError(host);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getRootMeanSquareError method, of class
     * AveragePowerEnergyPredictor.
     */
    @Test
    public void testGetRootMeanSquareError() {
        System.out.println("getRootMeanSquareError");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        double expResult = Double.MAX_VALUE;
        double result = instance.getRootMeanSquareError(host);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of toString method, of class AveragePowerEnergyPredictor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        instance.toString();
    }

    /**
     * Test of printFitInformation method, of class AveragePowerEnergyPredictor.
     */
    @Test
    public void testPrintFitInformation() {
        System.out.println("printFitInformation");
        AveragePowerEnergyPredictor instance = new AveragePowerEnergyPredictor();
        instance.printFitInformation(host);
    }

}
