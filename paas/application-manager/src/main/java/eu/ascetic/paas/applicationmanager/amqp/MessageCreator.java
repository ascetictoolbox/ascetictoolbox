package eu.ascetic.paas.applicationmanager.amqp;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.Application;
import eu.ascetic.paas.applicationmanager.model.Deployment;
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
 * This class has the logic to create messages to be sent to the AMQP
 *
 */
public class MessageCreator {

	/**
	 * It converts an Application object to a AMQP message to be sent
	 * @param application Application object to be converted
	 * @return the message object to be sent to an AMQP Broker.
	 */
	public static ApplicationManagerMessage fromApplication(Application application) {
		
		if(application == null) {
			return null;
		}
		
		ApplicationManagerMessage message = new ApplicationManagerMessage();
		
		message.setApplicationId(application.getName());
		
		if(application.getDeployments() != null && application.getDeployments().size() > 0) {
			completeMessage(message, application.getDeployments().get(0));
		}
		
		return message;
	}
	
	private static void completeMessage(ApplicationManagerMessage message, Deployment deployment) {
		message.setDeploymentId("" + deployment.getId());
		message.setStatus(deployment.getStatus());

		if(deployment.getVms() != null ) {
			for(VM vm : deployment.getVms()) {
				message.addVM(getMessageVM(vm));
			}
		}
	}
	
	/**
	 * Creates a message event from an application name and a Deployment object
	 * @param applicationName to which the deployment belongs
	 * @param deployment from which to extract the information
	 * @return the JSON representation of the message
	 */
	public static ApplicationManagerMessage fromDeployment(String applicationName, Deployment deployment) {
		if(applicationName == null || deployment == null) {
			return null;
		}
		
		ApplicationManagerMessage message = new ApplicationManagerMessage();
		message.setApplicationId(applicationName);
		
		completeMessage(message, deployment);
		
		return message;
	}
	
	private static eu.ascetic.paas.applicationmanager.amqp.model.VM getMessageVM(VM vm) {
		eu.ascetic.paas.applicationmanager.amqp.model.VM messageVM = new eu.ascetic.paas.applicationmanager.amqp.model.VM();
		messageVM.setIaasVmId(vm.getProviderVmId());
		messageVM.setOvfId(vm.getOvfId());
		messageVM.setStatus(vm.getStatus());
		messageVM.setVmId("" + vm.getId());
		
		return messageVM;
	}
	
	/**
	 * Creates an event message from an application name, Deployment object and Vm oject
	 * @param applicationName to which deployment and vm belogns
	 * @param deployment from which to extract the information
	 * @param vm from which to extract the information
	 * @return the message object to be sent to the message queue
	 */
	public static ApplicationManagerMessage fromVM(String applicationName, Deployment deployment, VM vm) {
		if(applicationName == null || deployment == null || vm == null) {
			return null;
		}
		
		ApplicationManagerMessage message = new ApplicationManagerMessage();
		message.setApplicationId(applicationName);
		message.setDeploymentId("" + deployment.getId());
		message.setStatus(deployment.getStatus());
		
		message.addVM(getMessageVM(vm));
		
		return message;
	}
}
