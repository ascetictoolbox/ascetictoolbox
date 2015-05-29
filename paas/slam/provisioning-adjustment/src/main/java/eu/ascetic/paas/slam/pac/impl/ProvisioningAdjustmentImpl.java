/**
 *  Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


/**
 * Copyright (c) 2008-2010, SLASOI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of SLASOI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SLASOI BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author         Beatriz Fuentes - fuentes@tid.es
 * @version        $Rev$
 * @lastrevision   $Date$
 * @filesource     $URL$
 */

package eu.ascetic.paas.slam.pac.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.slasoi.gslam.core.control.Policy;
import org.slasoi.gslam.pac.ProvisioningAndAdjustment;

import eu.ascetic.paas.slam.pac.PaasViolationChecker;
import eu.ascetic.paas.slam.pac.applicationmanager.ModelConverter;
import eu.ascetic.paas.slam.pac.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.slam.pac.applicationmanager.model.Deployment;


/**
 * DOMAIN SPECIFIC PAC.
 * 
 * @author Miguel Rojas (UDO)
 * 
 */
public class ProvisioningAdjustmentImpl extends ProvisioningAndAdjustment {
    /**
     * logger.
     */
    private static Logger logger = Logger.getLogger(ProvisioningAdjustmentImpl.class.getName());
    private static String configurationFileImpl = null;
    
	/*
	 * TODO
	 * retrieve url from configuration file
	 */
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    /**
     * Constructor.
     * 
     * @param configFile
     *            configuration file to be used by the PAC
     */
    public ProvisioningAdjustmentImpl(final String configFile) {
        super(configFile);
        logger.info("Creating Skeleton PAC...");
        logger.info("Searching for Application Startup Events on ActiveMQ (configfile)...");
    }

    /**
     * Default constructor.
     */
    public ProvisioningAdjustmentImpl() {
        logger.info("Creating Skeleton PAC...");
        logger.info("Searching for Application Startup Events on ActiveMQ...");
        init();
        retrieveApplicationEvents();
    }
    
    
    public void init() {
    	logger.debug("ASCETIC: ProvisioningAndAdjustment, init() method");
		configurationFileImpl = configurationFile;
		configurationFileImpl = configurationFileImpl.replace("/", System.getProperty("file.separator"));
		try {

			String initialPath = configurationFilesPath + configurationFile;
			logger.debug("Initial path = " + initialPath);
			String thePath = initialPath.replace("/", System.getProperty("file.separator"));
			logger.debug("The path = " + thePath);
			properties.load(new FileInputStream(thePath));
			PropertyConfigurator.configure(configurationFilesPath + properties.getProperty(LOG4J_FILE));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.debug("Properties file loaded...");
    }
    
    /**
     * 1. Gets 'application started' events from the message queue
     */
    private void retrieveApplicationEvents() {
    	try{
    	
    	// Getting JMS connection from the server

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();

        // need to setClientID value, any string value you wish
        connection.setClientID("PaaS SLAM Application Event Listener");

        
        connection.start();
        
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("app-manager.deployment.deployed");

        //need to use createDurableSubscriber() method instead of createConsumer() for topic
        // MessageConsumer consumer = session.createConsumer(topic);
        MessageConsumer consumer = session.createDurableSubscriber(topic,
                "PAAS SLAM");

        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        logger.info("ACTIVEMQ: Received message in the topic app-manager.deployment.deployed: "+ textMessage.getText());
                        
                        ApplicationManagerMessage amMessage = ModelConverter.jsonToApplicationManagerMessage(textMessage.getText());
                        
                        logger.info("Retrieving application Details from the Application Manager...");
                        String slaId = retrieveApplicationDetails(amMessage.getApplicationId(),amMessage.getDeploymentId());
                        
                        logger.info("Asking ApplicationMonitor to initiateMonitoring...");
	                    String topicId = initiateMonitoring(amMessage.getApplicationId(), amMessage.getDeploymentId(), slaId);
                        
                        logger.info("Creating an instance of Paas Violation Checker...");
                        new Thread(new PaasViolationChecker(topicId, amMessage.getApplicationId(), amMessage.getDeploymentId(), slaId)).start();
                        
                    }
                } catch (JMSException e) {
                    logger.error("Caught:" + e);
                    e.printStackTrace();
                }
            }
        };

        consumer.setMessageListener(listener);
        //connection.close();
        }catch(Exception e){
        	e.printStackTrace();
        	logger.error(e.getMessage());
//            System.err.println("NOT CONNECTED!!!");
        }
    }
    
    
    /**
     * 2. Retrieve application details from the application manager whenever an 
     * "application starting" event is catched
     */
    private String retrieveApplicationDetails(String appId, String deploymentId) {
    	String slaId = "";
    	
    	try {
    		HttpClient client = new DefaultHttpClient();
    		/*
    		 * TODO
    		 * retrieve url from configuration file
    		 */
			HttpGet request = new HttpGet("http://10.4.0.16/application-manager/applications/"+appId+"/deployments/"+deploymentId);
			HttpResponse response = client.execute(request);
			
			System.out.println("status: " + response.getStatusLine());
			System.out.println("headers: " + response.getAllHeaders());
			System.out.println("body:" + response.getEntity());
			
			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			logger.debug("Response string from the ApplicationManager: "+responseString);
			
			Deployment deployment = ModelConverter.xmlDeploymentToObject(responseString);
			logger.debug("Deployment Id: "+deployment.getId());
			logger.debug("SLA Agreement: "+deployment.getSlaAgreement());
			
			slaId = deployment.getSlaAgreement();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return slaId;
    }
    
    
    /**
     * 3. Asks the application monitor to initiate the monitoring on the given app, deployment
     *  and SLA
     * Returns the id of the topic where to read the measurements
     */
    private String initiateMonitoring(String appId, String deploymentId, String slaId) {
    	/*
    	 * TODO
    	 */
    	return "application-monitor.monitoring."+appId+".measurement";
    }
    
    
    
   

    /**
     * Triggers the execution of a provisioning plan.
     * 
     * @param plan
     *            the plan to be executed
     * @throws PlanFoundException
     *             if the plan has been already sent for execution
     * @throws PlanFormatException
     *             if the plan is not corrected built
     */
    public void executePlan(Plan plan) throws PlanFoundException, PlanFormatException {
        logger.info("Execute plan " + plan.getPlanId());
    }

    /**
     * Cancels the execution of a plan.
     * 
     * @param planId
     *            the identifier of the plan to be cancelled
     * @throws PlanNotFoundException
     *             if the plan is not being executed
     */
    public void cancelExecution(String planId) throws PlanNotFoundException {
        logger.info("Cancel plan " + planId);
    }

    /**
     * POC informs the PAC that a given action is being executed.
     * 
     * @param planId
     *            identifier of the plan affected by the action
     * @param action
     *            action being executed
     * @param estimatedTime
     *            estimation of time for the action to finish
     * @throws PlanNotFoundException
     *             if the plan is not under PAC's control
     */
    public void ongoingAction(String planId, Task action, long estimatedTime) throws PlanNotFoundException {
        logger.info("Ongoing action " + action.getActionName() + " affecting plan " + planId);
    }

    /**
     * Method to inform about the status of a given plan.
     * 
     * @param planId
     *            the identifier of the plan
     * @return the plan status.
     * @throws PlanNotFoundException
     */
    public Status getPlanStatus(String planId) throws PlanNotFoundException {
        logger.info("get status of plan " + planId);
        return Status.PROVISIONING;
    }

    /**
     * Method to query the LLMS database.
     * 
     * @param ServiceManagerId
     *            ID of the ServiceManager to forward the query
     * @param query
     *            database query
     * @return the result of the query
     */
    // To be changed once the ServiceManagers define the query interface
    public String queryMonitoringDatabase(String ServiceManagerId, String query) {
        logger.info("Executing query " + query + " in Service Manager " + ServiceManagerId);
        return "hello";
    }

    /**
     * Set policies to the PAC. To be used from the business layer.
     * 
     * @param policyClassType
     *            type of policy (Adjustment/Negotiation)
     * @param policies
     *            new policies
     * @return result of the action
     */
    public int setPolicies(String policyClassType, Policy[] policies) {
        logger.info("Setting policies ");
        for (Policy policy : policies) {
            logger.info(policy.toString());
        }

        return 0;
    }

    /**
     * Get the policies used by the PAC. To be used from the business layer.
     * 
     * @param policyClassType
     *            type of policy (Adjustment/Negotiation)
     * @return policies the PAC policies
     */
    public Policy[] getPolicies(String policyClassType) {
        logger.info("Getting policies");
        return null;
    }
}
