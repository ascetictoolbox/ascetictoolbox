package eu.ascetic.paas.applicationmanager.spreader;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import eu.ascetic.paas.applicationmanager.providerregistry.PRClient;
import eu.ascetic.providerregistry.model.Provider;

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
 * Creates the Spring Bean that it is going to listen to the different messages coming from the message queue...
 *
 */
@Service("SpreaderBeanService")
public class SpreaderBean {
	private static Logger logger = Logger.getLogger(SpreaderBean.class);
	
	public SpreaderBean() {
		logger.info("Initializing the Spreader IaaS Monitoring Bean");
		
		//Getting an initial list of providers
		PRClient prClient = new PRClient();
		List<Provider> providers = prClient.getProviders();
		
	}
	
}
