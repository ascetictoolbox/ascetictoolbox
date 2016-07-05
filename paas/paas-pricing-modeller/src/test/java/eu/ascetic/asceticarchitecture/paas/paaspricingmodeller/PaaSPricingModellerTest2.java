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
import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;
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


public class PaaSPricingModellerTest2{
   

	public PaaSPricingModellerTest2() {
		
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
    	PaaSPricingModeller paasmodeller = new PaaSPricingModeller();
    	VMinfo vm1 = new VMinfo(1,7680, 2, 32000, 0, 1);
    	Thread.sleep(10000);
    	VMinfo vm2 = new VMinfo(2,7680, 2, 32000, 0, 1);
    	Thread.sleep(10000);
    	VMinfo vm3 = new VMinfo(3,7680, 2, 32000, 0, 1);
    	Thread.sleep(10000);
    	LinkedList<VMinfo> test = new LinkedList<>();
        test.add(vm1);
        test.add(vm2);
        test.add(vm3);
        paasmodeller.initializeApp("1",1, test);
        Thread.sleep(20000);
        paasmodeller.resizeVM(1, 2, 4, 15360, 64000);
        Thread.sleep(20000);
        paasmodeller.removeVM(1, 2);
   }
}