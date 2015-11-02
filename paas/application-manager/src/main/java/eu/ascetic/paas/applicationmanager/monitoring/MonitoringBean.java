package eu.ascetic.paas.applicationmanager.monitoring;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.paas.applicationmanager.providerregistry.model.Provider;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * SpringBean to create the EnergyModeller
 *
 */
//@Service("EnergyModellerService")
public class MonitoringBean {
	private static Logger logger = Logger.getLogger(MonitoringBean.class);
	protected List<Provider> providers;
	
	
	public MonitoringBean() {
		// TODO Leer de la base de datos las VMs actuales activas
		
		// TODO Subscribirse de forma infinita al Active MQ para cada una de las colas que existen...MonitoringBean
		//       Necesito crear un topic subscriber para cada una de las liatas...
		
		// We get first the list of providers from the provider registry... 
		logger.info("READING LIST OF PROVIDERS...");
		PRClient prClient = new PRClient();
		providers = prClient.getProviders();
		
		// For each provider we subscribe to its AMQP queue... creating a durable topic listener.
		
		// TODO Método que permita rehacer esa lista de proveedores? 
		
		// Message format:
		/*
		 * 
####################################
   Message received for destination: vm.4b340a07-0978-43c5-97f5-859c67700a8e.item.power
   Message:

{"name":"power","value":2.7542,"units":"W","timestamp":1443530984}


####################################
   Message received for destination: vm.4b340a07-0978-43c5-97f5-859c67700a8e.item.cpu
   Message:

{"name":"cpu","value":0.0477,"units":"Mhz","timestamp":1442903319}

		 */
	}
}
