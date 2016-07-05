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

import java.util.TimerTask;

import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client.PricingModellerQueueServiceManager;





public class Testlistener extends TimerTask{

	PricingModellerQueueServiceManager  producer;
	
	public Testlistener(PricingModellerQueueServiceManager  producer) {
		this.producer = producer;
		
	}
	
	
	@Override
	public void run() {
		
		
	}

}

