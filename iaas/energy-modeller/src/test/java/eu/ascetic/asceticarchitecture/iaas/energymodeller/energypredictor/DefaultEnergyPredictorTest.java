package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DefaultDatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.EnergyUsagePrediction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultEnergyPredictorTest {

    public DefaultEnergyPredictorTest() {

    }

    public DefaultEnergyModelTrainer trainer = new DefaultEnergyModelTrainer();
    public Host host = new Host(10084, "asok10");
    public VM vm1 = new VM(2, 15048, 128);
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

        DefaultEnergyPredictor predictor = new DefaultEnergyPredictor();
        setCalibrationData(host);      
        host.setRamMb(32244);
        prediction = predictor.getHostPredictedEnergy(host, vms);
        System.out.println("Host: " + host.getHostName());
        System.out.println("VM Count: " + vms.size());
        System.out.println("store values size is: " + DefaultEnergyModelTrainer.storeValues.size());          
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }
    
    private void setCalibrationData(Host host) {
                DefaultDatabaseConnector db = new DefaultDatabaseConnector();
        host = db.getHostCalibrationData(host);

        if (host.getCalibrationData().isEmpty()) {
            System.out.println("WARNING: DB Data not setup correctly!");
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
        }
    }

    @Test
    public void TestGetVMPredictedEnergy() {
        System.out.println("getVMPredictedEnergy");
        EnergyUsagePrediction prediction;
        addVMs(vm1);
        addVMs(vm2);

        System.out.println("store values size is: " + DefaultEnergyModelTrainer.storeValues.size());

        DefaultEnergyPredictor predictor = new DefaultEnergyPredictor();
        setCalibrationData(host);
        host.setRamMb(32244);
        System.out.println("VM for Energy Prediction: " + vm1.toString());
        System.out.println("Amount of VMs Inducing Load: "+ vms.size());
        System.out.println("Host To Query: " + host.getHostName());
        prediction = predictor.getVMPredictedEnergy(vm1, vms, host);
        System.out.println("watts: " + prediction.getAvgPowerUsed() + " energy: " + prediction.getTotalEnergyUsed());

    }
}
