/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Richard
 */
public class CpuOnlySplinePolynomialEnergyPredictorTest {

    public CpuOnlySplinePolynomialEnergyPredictorTest() {
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
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testGetHostPredictedEnergy() {
        System.out.println("getHostPredictedEnergy");
        EnergyUsagePrediction prediction;

        addVMs(vm1);
        addVMs(vm2);

        CpuOnlySplinePolynomialEnergyPredictor predictor = new CpuOnlySplinePolynomialEnergyPredictor();
        host = setCalibrationData(host);
        host.setRamMb(32244);
        prediction = predictor.getHostPredictedEnergy(host, vms);
        System.out.println("Host: " + host.getHostName());
        System.out.println("VM Count: " + vms.size());
        System.out.println("store values size is: " + host.getCalibrationData().size());
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());
    }

    private Host setCalibrationData(Host host) {
        if (!host.isCalibrated()) {
            DefaultDatabaseConnector db = new DefaultDatabaseConnector();
            return db.getHostCalibrationData(host);
        }
        return host;
    }

    /**
     * Test of getVMPredictedEnergy method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testGetVMPredictedEnergy() {
        System.out.println("getVMPredictedEnergy");
        EnergyUsagePrediction prediction;
        addVMs(vm1);
        addVMs(vm2);

        System.out.println("store values size is: " + host.getCalibrationData().size());

        CpuOnlySplinePolynomialEnergyPredictor predictor = new CpuOnlySplinePolynomialEnergyPredictor();
        setCalibrationData(host);
        host.setRamMb(32244);
        System.out.println("VM for Energy Prediction: " + vm1.toString());
        System.out.println("Amount of VMs Inducing Load: " + vms.size());
        System.out.println("Host To Query: " + host.getHostName());
        prediction = predictor.getVMPredictedEnergy(vm1, vms, host);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }

    /**
     * Test of predictTotalEnergy method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testPredictTotalEnergy() {
        System.out.println("predictTotalEnergy");
        setCalibrationData(host);
        host.setRamMb(32244);
        double usageCPU = 0.0;
        TimePeriod timePeriod = null;
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        EnergyUsagePrediction expResult = null;
        EnergyUsagePrediction result = instance.predictTotalEnergy(host, usageCPU, timePeriod);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of predictPowerUsed method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testPredictPowerUsed_Host() {
        System.out.println("predictPowerUsed");
        setCalibrationData(host);
        host.setRamMb(32244);
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        double result = instance.predictPowerUsed(host);
        assert(result > 0.0);
    }

    /**
     * Test of predictPowerUsed method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testPredictPowerUsed_Host_double() {
        System.out.println("predictPowerUsed");
        setCalibrationData(host);
        host.setRamMb(32244);        
        double usageCPU = 0.1;
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        double result = instance.predictPowerUsed(host, usageCPU);
        assert(result > 0.0);
    }
    
     /**
     * Test of predictPowerUsed method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testPredictPowerUsedSweepTest() {
        System.out.println("predictPowerUsed");
        setCalibrationData(host);
        host.setRamMb(32244);        
        for (double usageCPU = 0.0; usageCPU <= 1.0; usageCPU = usageCPU + 0.05) {
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        double result = instance.predictPowerUsed(host, usageCPU);
        System.out.println(usageCPU + " " +result);
        assert(result > 0.0);        
        }
    }   

    /**
     * Test of getSumOfSquareError method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testGetSumOfSquareError() {
        System.out.println("getSumOfSquareError");
        setCalibrationData(host);
        host.setRamMb(32244);        
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        double result = instance.getSumOfSquareError(host);
        assert(result >= 0.0);
    }

    /**
     * Test of getRootMeanSquareError method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testGetRootMeanSquareError() {
        System.out.println("getRootMeanSquareError");
        setCalibrationData(host);
        host.setRamMb(32244);        
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        double result = instance.getRootMeanSquareError(host);
        assert(result >= 0.0);
    }

    /**
     * Test of toString method, of class CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        String result = instance.toString();
        assert(!result.equals(""));
    }

    /**
     * Test of printFitInformation method, of class
     * CpuOnlySplinePolynomialEnergyPredictor.
     */
    @Test
    public void testPrintFitInformation() {
        System.out.println("printFitInformation");
        setCalibrationData(host);
        host.setRamMb(32244);            
        CpuOnlySplinePolynomialEnergyPredictor instance = new CpuOnlySplinePolynomialEnergyPredictor();
        instance.printFitInformation(host);
    }

}
