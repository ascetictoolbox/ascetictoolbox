/**
 *  Copyright 2014 Athens University of Economics and Business
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller;


import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.AmqpClientPM;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.type.*;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;


import org.junit.After;
import org.junit.AfterClass;



import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PaaSPricingModellerTestqueue{
   

	public PaaSPricingModellerTestqueue() {
		
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
    public void testPriceEstimationBasic() throws Exception{
    	System.out.println("test PaaS");
    	PaaSPricingModeller prmodeller = new PaaSPricingModeller();

    	HashMap<Integer, Double> energyPerVM = new HashMap<>();
   // 	AmqpClientPM PMqueue1 = new AmqpClientPM();
	//	PMqueue1.setup(null,  "guest", "guest",  "Pricing");
	//	PricingModellerQueueServiceManager  producer1;
	//	producer1 = new PricingModellerQueueServiceManager(PMqueue1);
      //  VMinfo vm1 = new VMinfo(1,7680, 2, 32000, 10, 1, 0);
		VMinfo vm1 = new VMinfo(7680, 2, 32, 3600);
		VMinfo vm2 = new VMinfo(1, 7680, 2, 32, 3600, 0, "0");
    	VMinfo vm3 = new VMinfo(2,7680, 2, 32, 1, "1");
    	
    	LinkedList<VMinfo> test = new LinkedList<>();
        test.add(vm1);
        test.add(vm2);
        test.add(vm3);

        energyPerVM.put(2, 300.0);

      //  System.out.println(prmodeller.getAppPredictedPrice(1, 3600, test));
        
        prmodeller.initializeApp("e", 0, test);
        
       // System.out.println("----------------------------------------WAITING-----------------------------");
     //  prmodeller.initializeApp("e", 0, 0);
        
       System.out.println("----------------------------------------WAITING-----------------------------");
     //  Thread.sleep(40000);
       System.out.println("----------------------------------------NOW AGAIN-----------------------------");
      System.out.println("Energy" + prmodeller.getEventPredictedChargesOfApp(0,test,300));
       // System.out.println("The total charges until now are"+prmodeller.getAppTotalCharges(0, 0, 0));
       //System.out.println("----------------------------------------NOW AGAIN-----------------------------");
       //System.out.println("The total charges until now are"+prmodeller.getAppTotalCharges(0, 0, 0, energyPerVM));
    /*   prmodeller.resizeVM(0, 1, 2, 7680, 16);
       System.out.println("----------------------------------------WAITING-----------------------------");
       Thread.sleep(40000);
       System.out.println("----------------------------------------NOW AGAIN-----------------------------");
       System.out.println("The total charges until now are"+prmodeller.getAppTotalCharges(0, 0, 0));
       */
       
    //    test.add(vm2);
/*********************
  //      producer1.createConsumers("Pricing", "PMBILLING.DEPLOYMENTID.1.CHARGES");
  //      producer1.createConsumers("Pricing", "PMPREDICTION.DEPLOYMENTID.1.PRICEHOUR");
  //      producer1.createConsumers("Pricing", "PMBILLING.DEPLOYMENTID.1.TOTALCHARGES");
  //      producer1.createConsumers("Pricing", "PMBILLING.DEPLOYMENTID.1.VMID.1.CHARGES");
   //     producer1.createConsumers("Pricing", "PMBILLING.DEPLOYMENTID.1.VMID.1.TOTALCHARGES");
  //      producer1.createConsumers("Pricing", "PMPREDICTION.DEPLOYMENTID.1.VMID.1.PRICEHOUR");
     //   System.out.println("Charges predicted " +prmodeller.getAppTotalPredictedCharges(1, test));
      //  System.out.println("Charges predicted " +prmodeller.getAppAveragePredictedPrice(1, test));
       prmodeller.initializeApp("eleni",1, test);
     //   producer1.createConsumers("Pricing", "PMBILLING.DEPLOYMENTID.1.VMID.2.CHARGES");
        Thread.sleep(40000);
        System.out.println("-------------------------------------------------------------------------------------------------");
        VMinfo vm3 = new VMinfo(3,7680, 2, 32000, 10, 1, "0");
    	
    	LinkedList<VMinfo> test1 = new LinkedList<>();
        test1.add(vm3);
        System.out.println(prmodeller.predictAppPriceforNextHour(1, test1));
        //   prmodeller.addVM(1, vm2);
     //   Thread.sleep(10000);
     //   prmodeller.getCostlyVMs(1);
    //    System.out.println("-------------------------------------------------------------------------------------------------");
     //   prmodeller.resizeVM(1, 1, 4, 15960, 64000);
     //   prmodeller.removeVM(1, 1);
     //   System.out.println("charges: "+ prmodeller.getEventPredictedChargesOfApp(1, test, 3000));
      System.out.println("-------------------------------------------------------------------------------------------------");
     //   energyPerVM.put(1, 100.00);
     //   energyPerVM.put(2, 1000.00);
      //  System.out.println("the method pred returns: "+ prmodeller.getAppAveragePredictedPrice(1, test, 0));
      //  Thread.sleep(40000);
      //  System.out.println("the method here returns: "+ prmodeller.getAppTotalChargesPaaSCalculated(1));
        Thread.sleep(70000);
        *********************/
   }
    

}


