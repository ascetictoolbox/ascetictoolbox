package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultEnergyModelTrainerTest {

    public DefaultEnergyModelTrainerTest() {

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

    @Test
    public void testTrainModel() {
        DefaultEnergyModelTrainer trainer = new DefaultEnergyModelTrainer();
        EnergyModel model1;
        Host host = new Host(1, "testHost");
        boolean trained = false;
        double usageCPU;
        double usageRAM;
        double totalEnergyUsed;
        Random randomGenerator = new Random();

        for (int i = 1; i <= 5; i++) {
            usageRAM = (randomGenerator.nextInt(1000) / 1000d);
            usageCPU = (randomGenerator.nextInt(1000) / 1000d);
            totalEnergyUsed = (randomGenerator.nextInt(1000) / 1000d);

            trained = trainer.trainModel(host, usageCPU, usageRAM,
                    totalEnergyUsed, 5);

        }
        if (trained) {
            model1 = trainer.retrieveModel(host);
            System.out.println("model CPU coefficient: " + model1.getCoefCPU());
            System.out.println("model RAM coefficient: " + model1.getCoefRAM());
            System.out.println("model intercept : " + model1.getIntercept());
        }
    }

    @Test
    public void testTrainModelForPredictor() {
        DefaultEnergyModelTrainer trainer = new DefaultEnergyModelTrainer();
        Host host = new Host(1, "testHost");
        double usageCpu;
        double usageRam;
        double totalEnergyUsed;
        Random randomGenerator = new Random();

        for (int i = 1; i <= 5; i++) {
            usageRam = (randomGenerator.nextInt(1000) / 1000d);
            usageCpu = (randomGenerator.nextInt(1000) / 1000d);
            totalEnergyUsed = (randomGenerator.nextInt(1000) / 1000d);

            trainer.trainModel(host, usageCpu, usageRam,
                    totalEnergyUsed, 5);

        }
        System.out.println("training done");

    }

}
