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

import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.*;

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
    public void testPrediction() throws InterruptedException {
        System.out.println("test prediction");
        IaaSPricingModeller prModeller = new IaaSPricingModeller(null);
        /**
        System.out.println("Predicted charges for scheme 0:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 0, 3600L, "a1"));  
        System.out.println("Predicted price for scheme 0:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 0, 3600L, "a1")); 
        System.out.println("Predicted charges for scheme 1:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 1, 3600L, "a1"));  
        System.out.println("Predicted price for scheme 1:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 1, 3600L, "a1")); 
         **/
       // System.out.println("Predicted charges for scheme 2:" + prModeller.getVMChargesPrediction(2, 2, 20.0, 2, 3600L, "a1"));  
        //System.out.println("Predicted price for scheme 2:" + prModeller.getVMPricePerHourPrediction(2, 2, 20.0, 2, 3600L, "a1")); 
       
        prModeller.initializeVM("el", 1, "435e", "1");
        prModeller.initializeVM("el2", 1, "435e", "1");
      System.out.println("Final charges for VM:"+prModeller.getVMFinalCharges("el", true));
      System.out.println("Final charges for VM:"+prModeller.getVMFinalCharges("el", true));
        System.out.println("Final charges for APP:"+prModeller.getAppFinalCharges("1", true));
      //Prediction for Pricing Scheme 0
        
    }
	

}
