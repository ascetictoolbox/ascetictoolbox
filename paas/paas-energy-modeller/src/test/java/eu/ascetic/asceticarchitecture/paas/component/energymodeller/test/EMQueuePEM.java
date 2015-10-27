/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;


public class EMQueuePEM {
	
	private static AmqpClient paasQm;
	private static AmqpClient iaasQm;
	private static ApplicationRegistry registry;
	private static DataConsumptionHandler dataCollectorHandler;
	static EnergyModellerQueueServiceManager queueManager;
	

	 

	
	@BeforeClass
	public static void setup() {
		paasQm = new AmqpClient();
		iaasQm =  new AmqpClient();
		try {
			
			// dev enb
			//paasQm.setup("192.168.3.16:5673", "guest", "guest", "PEMENERGY");
			//iaasQm.setup("192.168.3.17:5673", "guest", "guest");
			// stable env
			paasQm.setup("192.168.3.222:5673", "guest", "guest", "PEMENERGY");
			iaasQm.setup("192.168.3.223:5673", "guest", "guest");
			registry = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://192.168.99.100:3306/ascetic_paas_em","root","root");
			dataCollectorHandler = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://192.168.99.100:3306/ascetic_paas_em","root","root");
			queueManager = new EnergyModellerQueueServiceManager(iaasQm,paasQm,registry,dataCollectorHandler);
			queueManager.createTwoLayersConsumers("APPLICATION.*.DEPLOYMENT.*.VM.*.*","vm.*.item.*");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void sendMessagePrediction() throws Exception {
		
		
		while (true){
			
		}
		

	}
	

}
