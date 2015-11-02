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
package eu.ascetic.asceticarchitecture.iaas.iaaspricingmodeller;

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
    public void testGetValues() {
/*     System.out.println("test get functions for all values");
        IaaSPricingModeller VMCost = new IaaSPricingModeller();
        
        double energyc=VMCost.getEnergyCost();
        double expenergyc = 0.07;
        assertEquals(expenergyc, energyc, 0.02);
        
        double asserc=VMCost.getAmortHostCost();
        double expasserc = 0.08;
        assertEquals(expasserc, asserc, 0.02);
        
        double PUEc=VMCost.getPUE();
        double expPUEc = 1.7;
        assertEquals(expPUEc, PUEc, 0.02);
    }
	
	@Test
    public void testConstructor() {
        System.out.println("test constructor");
        IaaSPricingModeller VMCost = new IaaSPricingModeller(0.05, 0.02, 1.2);
        
        double energyc=VMCost.getEnergyCost();
        double expenergyc = 0.05;
        assertEquals(expenergyc, energyc, 0.02);
        
        double asserc=VMCost.getAmortHostCost();
        double expasserc = 0.02;
        assertEquals(expasserc, asserc, 0.02);
        
        double PUEc=VMCost.getPUE();
        double expPUEc = 1.2;
        assertEquals(expPUEc, PUEc, 0.02);
  */  }
	
	@Test
    public void testSetValues() {
    /*    System.out.println("test set functions for all values");
        IaaSPricingModeller VMCost = new IaaSPricingModeller();
        
        VMCost.setEnergyCost(0.06);
        double energyc=VMCost.getEnergyCost();
        double expenergyc = 0.06;
        assertEquals(expenergyc, energyc, 0.02);
        
        VMCost.setAmortHostCost(0.01);
        double asserc=VMCost.getAmortHostCost();
        double expasserc = 0.01;
        assertEquals(expasserc, asserc, 0.02);
        
        VMCost.setPUE(1.3);
        double PUEc=VMCost.getPUE();
        double expPUEc = 1.3;
        assertEquals(expPUEc, PUEc, 0.02);
    */}
	
	@Test
    public void testGetVMCostEstimation() {
      /*  System.out.println("test VM cost estimation");
        IaaSPricingModeller VMCost = new IaaSPricingModeller();
        double totalEnergyUsed = 0.8;
        String hostId = "1";
        double VMCosts = VMCost.getVMCostEstimation(totalEnergyUsed, hostId);
        //cost=0.08+(0.07*0.8*1.7)=0.1752
        //price=0.1752+0.1752*20/100=0.21
        double expVMCosts = 0.17;
        assertEquals(expVMCosts, VMCosts, 0.02);
	*/}
	
	
	@Test
    public void testGetVMPriceEstimation() {
      /*  System.out.println("test VM price estimation");
        IaaSPricingModeller VMCost = new IaaSPricingModeller();
        double totalEnergyUsed = 0.8;
        String hostId = "1";
        double VMPrice = VMCost.getVMPriceEstimation(totalEnergyUsed, hostId);
        //cost=0.08+(0.07*0.8*1.7)=0.1752
        //price=0.1752+0.1752*20/100=0.21
        double expVMPrice = 0.21;
        assertEquals(expVMPrice, VMPrice, 0.02);
	*/}
}
