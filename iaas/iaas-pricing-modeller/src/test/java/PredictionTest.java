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


/**
 * This is the test of the pricing modeller of IaaS layer. 
 * @author E. Agiatzidou
 */

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;









import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.*;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.queue.Client;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.DynamicEnergyPrice;
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.types.EnergyPriceSetter;

public class PredictionTest 
{
   

	public PredictionTest() {
		
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
    public void testPrediction() throws Exception {
		class readmsg extends TimerTask{
			 AmqpBasicListener listener;
			 String previousmsg = null;
			public readmsg( AmqpBasicListener listener){
				this.listener = listener;
			}
			
			@Override
			public void run() {

				try {
					String newmsg = listener.getMessage();
					if (newmsg!=null){
						if (newmsg!=previousmsg){
							System.out.println(newmsg);
							previousmsg = newmsg;
						}
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
       
        IaaSPricingModeller prModeller = new IaaSPricingModeller(null);
     //  AmqpMessageReceiver receiver = new AmqpMessageReceiver("localhost:5672", "guest", "guest", "test.topic2",true);
      AmqpBasicListener listener = new AmqpBasicListener();
     //  receiver.setMessageConsumer(listener);
        
       Client queue = new Client();
        queue.setup("localhost:5672", "guest", "guest");
        queue.registerListener("test.topic2", listener);
        Timer timer = new Timer (true);
        long delay = 20;
		timer.scheduleAtFixedRate(new readmsg(listener), TimeUnit.SECONDS.toMillis(delay), 100);
        /**
        System.out.println("Predicted charges for scheme 0:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 0, 3600L, "a1"));  
        System.out.println("Predicted price for scheme 0:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 0, 3600L, "a1")); 
        System.out.println("Predicted charges for scheme 1:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 1, 3600L, "a1"));  
        System.out.println("Predicted price for scheme 1:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 1, 3600L, "a1")); 
         **/
       // System.out.println("Predicted charges for scheme 2:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 2, 3600L, "a1"));  
        //System.out.println("Predicted price for scheme 2:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 2, 3600L, "a1")); 

        prModeller.initializeVM("el", 0, "435e", "1");

       // prModeller.initializeVM("el2", 1, "435e", "1");
     Thread.sleep(10000);
   //  listener.onMessage(null);

   //    System.out.println("Initial charges for VM:"+prModeller.getVMFinalCharges("el", false));
   // Thread.sleep(20000);
   // System.out.println(listener.getMessage());

   //  Thread.sleep(20000);
   //  System.out.println(listener.getMessage());    
      System.out.println("Final charges for VM:"+prModeller.getVMFinalCharges("el", true));
    //   System.out.println("Final charges for APP:"+prModeller.getAppFinalCharges("1", true));
      //Prediction for Pricing Scheme 0

    //   Thread.sleep(1000);
       
    }
	

}
