package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;

public class DefaultEnergyPredictorTest {

    public DefaultEnergyPredictorTest() {

    }

    public DefaultEnergyModelTrainer trainer = new DefaultEnergyModelTrainer();
    public Host host = new Host(2, "testHost");
    public VM vm1 = new VM(2, 4, 128);
    public VM vm2 = new VM(4, 8, 256);
    public Collection<VM> VMs = new ArrayList<>();

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
        VMs.add(vm);
    }

    @Test
    public void TestGetHostPredictedEnergy() {
        EnergyUsagePrediction prediction;

        addVMs(vm1);
        addVMs(vm2);

        double usageCPU;
        double usageRAM;
        double totalEnergyUsed;
        Random randomGenerator = new Random();

        for (int i = 1; i <= 5; i++) {
            usageRAM = (randomGenerator.nextInt(1000) / 1000d);
            usageCPU = (randomGenerator.nextInt(1000) / 1000d);
            totalEnergyUsed = (randomGenerator.nextInt(1000) / 1000d);
            trainer.trainModel(host, usageCPU, usageRAM, totalEnergyUsed, 5);
        }
        System.out.println("store values size is: " + trainer.storeValues.size());

        DefaultEnergyPredictor predictor = new DefaultEnergyPredictor();
        prediction = predictor.getHostPredictedEnergy(host, VMs);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }

    @Test
    public void TestGetVMPredictedEnergy() {
        EnergyUsagePrediction prediction;
        addVMs(vm1);
        addVMs(vm2);

        System.out.println("store values size is: " + trainer.storeValues.size());

        DefaultEnergyPredictor predictor = new DefaultEnergyPredictor();
        System.out.println(vm1.toString());
        System.out.println(VMs.size());
        System.out.println(host.getHostName());
        prediction = predictor.getVMPredictedEnergy(vm1, VMs, host);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }
}
