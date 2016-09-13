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

import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
//import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
import eu.ascetic.asceticarchitecture.paas.type.*;

import java.io.StringWriter;
import java.util.LinkedList;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PaaSPricingModellerTest{
   

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
    public void testPriceEstimationBasic() throws Exception{
    	System.out.println("test PaaS");
    	PaaSPricingModeller prmodeller = new PaaSPricingModeller();
    	
    	//AmqpMessageReceiver receiver = new AmqpMessageReceiver("localhost:5672", "guest", "guest", "test.topic2",true);
       // AmqpBasicListener listener = new AmqpBasicListener();
       // receiver.setMessageConsumer(listener);
    	//prmodeller.getAppPredictedCharges(1, 0, 10);
    	//System.out.println(prmodeller.getAppPredictedCharges(1, 0, 10));
    	//Thread.sleep(1000);
    	//System.out.println(listener.getMessage());
	   	
	  // 	System.out.println(prmodeller.getAppPredictedPrice(1, 0, 10,7200));
    	//prmodeller.initializeApp(1, 0);
    	//System.out.println(prmodeller.getAppTotalCharges(1, 1, 4));
    	
    	//System.out.println(prmodeller.getEventPredictedCharges(1, 2, 2048, 40000.0, 51.7, 1, (long)6635.77, 2));

    
    	VMinfo vm1 = new VMinfo(0,1024, 2, 50000, 3600,"0");
    	VMinfo vm2 = new VMinfo(1,1024, 2, 50000, 3600, "1");
    	LinkedList<VMinfo> test = new LinkedList<>();
        test.add(vm1);
        test.add(vm2);
      //  prmodeller.initializeApp(1, test, 1);
        //System.out.println(prmodeller.getEventPredictedChargesOfApp(1, test, 10));
        Thread.sleep(10000);
     // System.out.println(listener.getMessage());
     //   System.out.println(prmodeller.getAppTotalChargesPaaSCalculated(1));
        Thread.sleep(40000);
     //   System.out.println(prmodeller.getAppTotalChargesPaaSCalculated(1));
        
//=======
    	/*VMinfo vm11 = new VMinfo(1024, 2, 50000, 3600);
    	VMinfo vm22 = new VMinfo(1024, 1, 50000, 3600);
    	LinkedList<VMinfo> test1 = new LinkedList<>();
    	test.add(vm1);
    	test.add(vm2);
    	System.out.println(prmodeller.getEventPredictedChargesOfApp(1, test, 10, 0));*/

    
   }

    /*<-------------------------------------start activemq tests---------------------------------------------------------->*/    
   @Test
    // The code to check if the message is sended...
    public void testingSendAndRecieve() throws Exception {
          //  AmqpMessageReceiver receiver = new AmqpMessageReceiver("localhost:5672", "guest", "guest", "test.topic2",true);
           // AmqpBasicListener listener = new AmqpBasicListener();
           // receiver.setMessageConsumer(listener);

           // AmqpMessageProducer producer = new AmqpMessageProducer("localhost:5672", "guest", "guest", "test.topic2",true);
            //producer.sendMessage("TestX");
           
          //  Thread.sleep(1000l);
           
           // System.out.println(listener.getMessage());
     
    }
}
   /*    @Test
    // The code to check if the message is sended...
    public void testingSendAndRecieve() throws Exception {
            AmqpMessageReceiver receiver = new AmqpMessageReceiver("localhost:5672", "guest", "guest", "id"+"."+"2",true);
            AmqpBasicListener listener = new AmqpBasicListener();
            receiver.setMessageConsumer(listener);
           
            StringWriter data = new StringWriter();
        	FirstMessage message = new FirstMessage();
        	message.setId("2");
        	//message.setPrice("33");
        	data.write(message.toString());
        	JsonObject objwr = Json.createObjectBuilder().add("id", message.getId()).build();//add("price",message.getPrice()).
        	
            AmqpMessageProducer producer = new AmqpMessageProducer("localhost:5672", "guest", "guest", objwr.toString(),true);
            producer.sendMessage(objwr.toString());
           
            Thread.sleep(1000l);
           
            System.out.println(listener.getMessage());
    }*/
    
/*<-------------------------------------end activemq tests---------------------------------------------------------->*/    
   
