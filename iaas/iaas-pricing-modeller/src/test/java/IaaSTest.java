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

public class IaaSTest 
{
   

	public IaaSTest() {
		
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
      
        IaaSPricingModeller prModeller = new IaaSPricingModeller(2, null);
     
        prModeller.initializeVM("VMID1", 1, "Host1", "APPID1");

    
        Thread.sleep(50000);
        
        prModeller.getAppFinalCharges("APPID1", true);
        
        
        
        
    }
	

}
