package eu.ascetic.paas.applicationmanager.amqp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.paas.applicationmanager.dao.ApplicationDAO;
import eu.ascetic.paas.applicationmanager.dao.DeploymentDAO;
import eu.ascetic.paas.applicationmanager.dao.VMDAO;
import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.Dictionary;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;

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
 */

public class AmqpFakeMessageTestIT {

	public static void main(String args[]) throws InterruptedException {
		// Load Spring configuration
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("/mysql-jpa-test-configuration.xml");
		ApplicationDAO applicationDAO = (ApplicationDAO) context.getBean("ApplicationService");	
		DeploymentDAO deploymentDAO = (DeploymentDAO) context.getBean("DeploymentService");
		VMDAO vmDAO = (VMDAO) context.getBean("VMService");
		
		
		// We read the deployment from the db
		Deployment deployment = deploymentDAO.getById(1300);
		System.out.println("SLA ID: " + deployment.getSlaUUID());
		
		//We build the message...
		ApplicationManagerMessage amMessage = MessageCreator.fromDeployment(deployment.getApplication().getName(), deployment, Dictionary.APPLICATION_STATUS_DEPLOYED);
		String messageString = ModelConverter.applicationManagerMessageToJSON(amMessage);
		System.out.println(messageString);
		
		// We send the message
		Configuration.enableAMQP = "yes";
		Configuration.amqpAddress = "localhost:5673";
		Configuration.amqpUsername = "guest"; 
		Configuration.amqpPassword = "guest";
		
		AmqpProducer.sendDeploymentDeployedMessage(deployment.getApplication().getName(), deployment);
		
		System.exit(0);
	}
}
