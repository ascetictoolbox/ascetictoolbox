package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class DefaultEnergyModelTrainerTest 
{
   

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
    	EnergyModel model1=new EnergyModel();
    	EnergyModel model2=new EnergyModel();
    	Host host= new Host(1, "testHost");
    	Host host2= new Host(2, "testHost2");
    	boolean trained=false;
    	double usageCPU=0.0;
    	double usageRAM=0.0;
    	double totalEnergyUsed=0.0;
    	Random randomGenerator = new Random();
    	for (int i=1; i<=10; i++){
    		usageRAM=randomGenerator.nextDouble();
    		usageCPU=randomGenerator.nextDouble();
    		totalEnergyUsed=randomGenerator.nextDouble();
    		TimePeriod duration = new TimePeriod(new GregorianCalendar(), 1, TimeUnit.MINUTES);
    		trained=trainer.trainModel (host, usageCPU, usageRAM, totalEnergyUsed, 10, duration);
    		trainer.trainModel (host2, usageCPU, usageRAM, totalEnergyUsed, 10, duration);
    		
    	}
    	if (trained){
    	model2=trainer.retrieveModel(host2);
		System.out.println("model: "+ model2.getCoefCPU());
    	}
    }
}