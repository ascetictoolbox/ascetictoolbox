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

/**
* This is the test of the pricing modeller of PaaS layer. 
* 
* @author E. Agiatzidou
*/

import eu.ascetic.asceticarchitecture.paas.type.*;

import java.util.LinkedList;

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
    	System.out.println("test PaaS");
    	PaaSPricingModeller prmodeller = new PaaSPricingModeller();
    	//prmodeller.getAppPredictedCharges(1, 0, 10);
    	//prmodeller.getAppPredictedPrice(1, 0, 10,7200);
    	//prmodeller.initializeApp(1, 0);
    	//prmodeller.getEventPredictedCharges(1, 2, 2048, 40000.0, 51.7, 1, (long)6635.77, 2);
    	//VMinfo vm1 = new VMinfo(1024, 2, 50000, 3600);
    	//VMinfo vm2 = new VMinfo(1024, 1, 50000, 3600);
       // LinkedList<VMinfo> test = new LinkedList<>();
       // test.add(vm1);
       // test.add(vm2);
      //  System.out.println(prmodeller.getEventPredictedChargesOfApp(1, test, 10, 0));
    
   }
}
