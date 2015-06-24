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
        System.out.println("test IaaS Pricing Modeller");
        IaaSPricingModeller prModeller = new IaaSPricingModeller(null);
      //  System.out.println("Energy Provider: " +prModeller.getEnergyProvider().getId());
       // System.out.println("IaaS Provider: " +prModeller.getIaaSId());
     //  System.out.println("Dynamic Energy Price: " +prModeller.getEnergyProvider().getDynamicEnergyPrice().getEnergyPriceOnly());
      //  System.out.println("Dynamic Time: " +prModeller.getEnergyProvider().getDynamicEnergyPrice().getTimeOnly().getTime());
        
     //  prModeller.initializeVM(1, 1.0, 2.0, 250.0, 0);
     //  prModeller.initializeVM(2, 2.0, 2.0, 250.0, 1);
    //   prModeller.getVMCurrentEnergyCharges(2);
        Thread.sleep(22000);
        //to test the billing
      //  prModeller.getVMCurrentEnergyCharges(2);
        
    }
	

}
