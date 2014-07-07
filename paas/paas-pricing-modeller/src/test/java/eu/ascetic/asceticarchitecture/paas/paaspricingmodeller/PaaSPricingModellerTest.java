package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;


import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PaaSPricingModellerTest 
{
   

	public PaaSPricingModellerTest() {
		
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
    public void testPriceEstimationBasic(){
    	 System.out.println("test basic function for price estimation ");
    	 PaaSPricingModeller priceApp = new PaaSPricingModeller();
    	 double totalEnergyUsed=0.8;
    	 int deploymentId=1;
    	 int appId=1;
    	 int iaasId=1;
    	 double iaasPrice=0.21;
    	 double price=priceApp.getAppPriceEstimation(totalEnergyUsed, deploymentId, appId, iaasId, iaasPrice);
    	 //  price=iaasPrice + iaasPrice*20/100;
    	 double expectedPrice = 0.25;
    	 assertEquals(expectedPrice, price, 0.02);
    }
	    
}
