/*  Copyright 2015 Athens University of Economics and Business
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

package eu.ascetic.asceticarchitecture.paas.type;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.billing.PaaSPricingModellerBilling;



public class ChargesCalculator extends TimerTask{
	PaaSPricingModellerBilling billing;
	DeploymentInfo depl;
	Logger logger = Logger.getLogger(ChargesCalculator.class);
	
	public ChargesCalculator(DeploymentInfo depl, PaaSPricingModellerBilling billing) {
		this.billing = billing;
		this.depl = depl;
	}
	
	@Override
	public void run() {
		
		
		try {
			synchronized(depl.getLock()){
			depl.setChanging(true);
			//System.out.println("ChargesCalculator calculation of charges");
			logger.info("ChargesCalculator calculation of charges");
			billing.getAppCurrentTotalCharges(depl.getId());
			depl.setChanging(false);
			depl.getLock().notifyAll();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
}