package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;


import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.type.PaaSPrice;

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
	
    @Test
    public void testPriceEstimationWithoutEnergy(){
    	 System.out.println("test function for price estimation that does not take into account energy");
    	 PaaSPricingModeller priceApp = new PaaSPricingModeller();
    	 int deploymentId=1;
    	 int appId=1;
    	 int iaasId=1;
    	 double iaasPrice=0.21;
    	 double price=priceApp.getAppPriceEstimation( deploymentId, appId, iaasId, iaasPrice);
    	 //  price=iaasPrice + iaasPrice*20/100;
    	 double expectedPrice = 0.25;
    	 assertEquals(expectedPrice, price, 0.02);
    }
    
    @Test
    public void testPriceEstimationWithoutIaasPrice(){
    	 System.out.println("test function for price estimation without IaaS Price");
    	 PaaSPricingModeller priceApp = new PaaSPricingModeller();
    	 PaaSPrice priceObj1= new PaaSPrice(0.5,1,1,1,0.23);
    	 PaaSPrice priceObj2= new PaaSPrice(0.2,2,2,2,0.35);
    	 priceApp.prices.add(priceObj1);
    	 priceApp.prices.add(priceObj2);
    	 double totalEnergyUsed=0.5;
    	 int deploymentId=1;
    	 int appId=1;
    	 int iaasId=1;
    	 double price1=priceApp.getAppPriceEstimation(totalEnergyUsed, deploymentId, appId, iaasId);
    	 //  price=iaasPrice + iaasPrice*20/100;
    	 double expectedPrice1 = 0.27;
    	 assertEquals(expectedPrice1, price1, 0.02);
    	 double price2=priceApp.getAppPriceEstimation(totalEnergyUsed, 2, appId, iaasId);
    	 double expectedPrice2 = 0.24;
    	 assertEquals(expectedPrice2, price2, 0.02);
    }
}
