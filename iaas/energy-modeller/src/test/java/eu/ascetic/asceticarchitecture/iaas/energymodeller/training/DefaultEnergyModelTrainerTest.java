/**
 * Copyright 2014 Athens University of Economics and Business
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
