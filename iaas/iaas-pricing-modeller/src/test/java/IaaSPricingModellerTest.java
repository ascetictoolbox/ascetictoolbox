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

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.IaaSPricingModeller;


public class IaaSPricingModellerTest 
{
   

	public IaaSPricingModellerTest() {
		
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
    public void testIaaSPricingModeller() throws InterruptedException {
      //  System.out.println("test IaaS Pricing Modeller");
        IaaSPricingModeller prModeller = new IaaSPricingModeller(null);
      
       // System.out.println("Dynamic Energy Price: " +prModeller.getEnergyProvider().getNewDynamicEnergyPrice().getPriceOnly());
        System.out.println("Predicted value for scheme 0:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 1, 360L, "2a"));  
        
      System.out.println("Initialize VM");
       prModeller.initializeVM("sm", 1);        
        Thread.sleep(10000);
        prModeller.getVMCurrentCharges("sm");
     //   Thread.sleep(10000);
     //   prModeller.getVMCurrentCharges("sm");
     //  prModeller.initializeVM(2, 2.0, 2.0, 250.0, 1);
    //   prModeller.getVMCurrentEnergyCharges(2);
        
        //to test the billing
      //  prModeller.getVMCurrentEnergyCharges(2);
        
    }
	

}
