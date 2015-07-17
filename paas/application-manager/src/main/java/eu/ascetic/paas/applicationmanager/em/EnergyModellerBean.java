package eu.ascetic.paas.applicationmanager.em;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.builder.EnergyModellerFactory;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.interfaces.PaaSEnergyModeller;

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
@Component
@Scope("request")
public class EnergyModellerBean {
	private static Logger logger = Logger.getLogger(EnergyModellerBean.class);
	private PaaSEnergyModeller energyModeller;

	public EnergyModellerBean() {
		logger.debug("Initializing Energy Modeller...");
		// TODO this path here looks ugly, move it to the configuration file...
		energyModeller = EnergyModellerFactory.getEnergyModeller("/etc/ascetic/paas/em/config.properties");
	}

	public PaaSEnergyModeller getEnergyModeller() {
		return energyModeller;
	}
	
	// To force the construction of the bean... 
	@PostConstruct
    public void init() {

    }
}
