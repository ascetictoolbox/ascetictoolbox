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

import eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller.energyprovider.EnergyProvider;


public class EnergyProviderTest
{
   

	public EnergyProviderTest() {
		
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
    public void testEnergyProvider() throws InterruptedException {
        System.out.println("test energy provider");
        EnergyProvider provider = new EnergyProvider();
        
       /* for (int i=0;i==7000; i++){
        	System.out.print(i);
        	if (i==0 || i==1000||i==6000){
        		System.out.println("id " + provider.getId()+ " dynamic price "+ provider.getDynamicEnergyPrice().getEnergyPriceOnly());
        	}
        }*/
    	//double price = provider.getDynamicEnergyPrice().getEnergyPriceOnly();
    	//System.out.println("id " + provider.getId()+ " dynamic price "+ provider.getDynamicEnergyPrice().getEnergyPriceOnly());
    	//Thread.sleep(22000);
    	//price = provider.getDynamicEnergyPrice().getEnergyPriceOnly();
    	//System.out.println("id " + provider.getId()+ " dynamic price "+ provider.getDynamicEnergyPrice().getEnergyPriceOnly());
     	
    }
	
}
	


