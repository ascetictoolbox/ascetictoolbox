package eu.ascetic.asceticarchitecture.iaas.energymodeller.training;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.TimePeriod;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.usage.HostEnergyCalibrationData;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energymodel.EnergyModel;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.training.DefaultEnergyModelTrainer;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.ParseException;
import java.text.DecimalFormat;

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
    	
    	for (int i=1; i<=5; i++){
    		usageRAM= (randomGenerator.nextInt(1000)/1000d);
    		usageCPU=(randomGenerator.nextInt(1000)/1000d);
    		totalEnergyUsed=(randomGenerator.nextInt(1000)/1000d);
    		
    		trained=trainer.trainModel (host, usageCPU, usageRAM, totalEnergyUsed, 5);
    		//trainer.trainModel (host2, usageCPU, usageRAM, totalEnergyUsed, 10, duration);
    		
    	}
    	if (trained){
    	model1=trainer.retrieveModel(host);
		System.out.println("model CPU coefficient: "+ model1.getCoefCPU());
		System.out.println("model RAM coefficient: "+ model1.getCoefRAM());
		System.out.println("model intercept : "+ model1.getIntercept());
    	}
    }
    
    @Test   
    public void testTrainModelForPredictor() {
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
    	
    	for (int i=1; i<=5; i++){
    		usageRAM= (randomGenerator.nextInt(1000)/1000d);
    		usageCPU=(randomGenerator.nextInt(1000)/1000d);
    		totalEnergyUsed=(randomGenerator.nextInt(1000)/1000d);
    		
    		trained=trainer.trainModel (host, usageCPU, usageRAM, totalEnergyUsed, 5);
    		//trainer.trainModel (host2, usageCPU, usageRAM, totalEnergyUsed, 10, duration);
    		
    	}
    	System.out.println("training done");
    	
    	

    }
    

    
}