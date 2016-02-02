package eu.ascetic.paas.applicationmanager.em;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;
import eu.ascetic.paas.applicationmanager.conf.Configuration;


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
@Service("EnergyModellerService")
public class EnergyModellerBean {
	private static Logger logger = Logger.getLogger(EnergyModellerBean.class);
	private PaaSEnergyModeller energyModeller;

	public EnergyModellerBean() {
		logger.info("Initializing Energy Modeller...");
		logger.info("Config file for EM: " + Configuration.emConfigurationFile);
    	
		try {
			energyModeller = EnergyModellerFactory.getEnergyModeller(Configuration.emConfigurationFile);
		} catch(Exception e) {
			logger.info("Not possible to load Energy Modeller");
			energyModeller = null;
		}
	}

	public PaaSEnergyModeller getEnergyModeller() {
		return energyModeller;
	}
}
