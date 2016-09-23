package eu.ascetic.paas.applicationmanager.pm;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.ascetic.asceticarchitecture.paas.type.VMinfo;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.VM;

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
@Service("PriceModellerService")
public class PriceModellerBean implements InitializingBean {
	private static Logger logger = Logger.getLogger(PriceModellerBean.class);
	private PriceModellerClient priceModellerClient;
	@Autowired
	private DeploymentDAO deploymentDAO;

	public PriceModellerBean() {}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		logger.info("Initializing Price Modeller...");
    	
		priceModellerClient = PriceModellerClient.getInstance();
		
		logger.info("Loading information from actual running deployments using DeploymentDAO: " + deploymentDAO);
		if(deploymentDAO != null) {
			List<Deployment> deployments = deploymentDAO.getDeploymentsWithStatus(Dictionary.APPLICATION_STATUS_DEPLOYED);

			for(Deployment deployment : deployments) {
				logger.info("Adding information of deployment: " + deployment.getId() + " of application: " + deployment.getApplication().getName());
				LinkedList<VMinfo> vmInfos = new LinkedList<VMinfo>();

				for(VM vm : deployment.getVms()) {

					int priceSchema = 0;
					if(vm.getPriceSchema() != null) {
						priceSchema = vm.getPriceSchema().intValue();
					}

					logger.info("Adding information of VM: " + vm.getId() + ", RAM:" + vm.getRamActual() + ", CPU:" + vm.getCpuActual() + ", Disk:" +  vm.getDiskActual() + ", Schema:" + priceSchema + ", ProviderID:" + vm.getProviderId());

					VMinfo vmInfo = new VMinfo(vm.getId(), (double) vm.getRamActual(), (double) vm.getCpuActual(), (double) vm.getDiskActual(), priceSchema, vm.getProviderId());
					vmInfos.add(vmInfo);
				}

				priceModellerClient.initializeApplication(deployment.getApplication().getName(), deployment.getId(), vmInfos);
			}
		} else {
			logger.error("No possible to connect with DB, deploymentDAO is null!!!!");
		}
	}

	public PriceModellerClient getPriceModeller() {
		return priceModellerClient;
	}
}