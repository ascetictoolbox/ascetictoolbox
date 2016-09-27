package eu.ascetic.paas.applicationmanager.em.amqp;

import org.junit.Test;

import eu.ascetic.paas.applicationmanager.conf.Configuration;

/**
 * 
 * Copyright 2016 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Garcia Perez. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.garciaperez@atos.net 
 * 
 * Unit test that verifies the right work of the class EnergyModellerController
 *
 */
public class EnergyModellerQueueControllerIT {

	@Test
	public void integrationTestWithAnActiveAMQP() throws Exception {
		Configuration.amqpAddress = "localhost:5673";
		Configuration.amqpUsername = "guest";
		Configuration.amqpPassword = "guest";
		Configuration.emMeasurementsTopic = "MEASUREMENTS";
		Configuration.emPredictionsTopic = "PREDICTION";
		
		EnergyModellerQueueController emController = new EnergyModellerQueueController();
		emController.afterPropertiesSet();
		
		Thread.sleep(90000);
		
		System.out.println("####################################################################");
		System.out.println("####################################################################");
		System.out.println("####################################################################");
		System.out.println("SIZE: " + emController.predictionsMessages.size());
		System.out.println("Message for secKey: newsAssetSimple-Search-Light-Load-testmanual11018289628972898SEC : " + emController.getPredictionMessage("newsAssetSimple-Search-Light-Load-testmanual11018289628972898SEC"));
		System.out.println("####################################################################");
		System.out.println("####################################################################");
		System.out.println("####################################################################");
	}
}
