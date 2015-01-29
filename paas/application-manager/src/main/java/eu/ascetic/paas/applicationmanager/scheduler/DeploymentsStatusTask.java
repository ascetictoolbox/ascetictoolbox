package eu.ascetic.paas.applicationmanager.scheduler;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
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
 * This periodical taks will go through the deployments table, and look the ones that need some update
 * to be moved to a new state
 *
 */

public class DeploymentsStatusTask {
	private static Logger logger = Logger.getLogger(DeploymentsStatusTask.class);
	
	@Scheduled(fixedDelay=5000)
	public void checkDeploymentStatus() {
		logger.info("Retrieving application deployments from DB to see if it is necessary to move applications to other state");
		
	}	
}
