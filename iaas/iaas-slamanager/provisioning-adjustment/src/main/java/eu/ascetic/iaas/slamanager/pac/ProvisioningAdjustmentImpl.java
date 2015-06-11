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

package eu.ascetic.iaas.slamanager.pac;

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
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.slasoi.gslam.core.control.Policy;
import org.slasoi.gslam.pac.Agent;
import org.slasoi.gslam.pac.Event;
import org.slasoi.gslam.pac.EventType;
import org.slasoi.gslam.pac.ProvisioningAndAdjustment;

import eu.ascetic.iaas.slamanager.pac.vmm.VmManagerClient;
import eu.ascetic.iaas.slamanager.pac.vmm.models.VmDeployed;

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
	
    protected static String ACTIVEMQ_URL = "activemq_url";
    private static String ACTIVEMQ_CHANNEL = "activemq_channel";
    private static String DEPLOYED_VMS_QUEUE = "deployed_vms_queue";
    private static String VMMANAGER_URL = "vmmanager_url";

	// private SlaParser slaParser=null;

	private static String configurationFileImpl = null;

	/**
	 * Constructor.
	 * 
	 * @param configFile
	 *            configuration file to be used by the PAC
	 */
	public ProvisioningAdjustmentImpl(final String configFile) {
		super(configFile);
		// configurationFileImpl=configFile;
		logger.debug("Constructor ASCETIC PAC - file config=" + configFile);
		logger.info("Creating Skeleton PAC...");
	}

	/**
	 * Default constructor.
	 */
	public ProvisioningAdjustmentImpl() {
		// super();
		logger.debug("Constructor default ASCETIC PAC");
		// init();
		logger.info("Creating Skeleton PAC...");
		init();
		logger.info("Searching for VM Startup Events on ActiveMQ...");
		retrieveVmEvents();
	}

	private void createAgents() {
		// At the moment, only one agent is created
		manager = new Agent(configurationFilesPath + properties.getProperty(AGENT_CONFIGURATION_FILE));
		// SharedKnowledgePlane.getInstance(this.pacID).getStatefulSession().insert(manager);
		// SharedKnowledgePlane.getInstance(this.pacID).getStatefulSession().fireAllRules();
		addManagedElements(manager);
		new Thread(manager).start();
	}

	/**
	 * OVERLOAD initialization:
	 * 
	 * - load properties
	 * 
	 * - create agents
	 * 
	 * - initialize drools knowledge base.
	 */

	public void init() {

		logger.debug("ASCETIC: ProvisioningAndAdjustment, init() method");
//		configurationFileImpl = configurationFile;
//		configurationFileImpl = configurationFileImpl.replace("/", System.getProperty("file.separator"));
		
		String sepr = System.getProperty("file.separator");
		String confPath = System.getenv("SLASOI_HOME");
		String configFile = confPath + sepr
				+ "ascetic-iaas-slamanager" + sepr + "provisioning-adjustment" + sepr
				+ "provisioning_adjustment.properties";
		try {

//			String initialPath = configurationFilesPath + configurationFile;
			String initialPath = configFile;		
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

//		// SharedKnowledgePlane.getInstance(this.pacID).initKnowledgeBase(properties.getProperty(LOG_MODE_DROOLS));
//		String rules_path = configurationFilesPath + properties.getProperty(INITIAL_RULES_FILE);
//		rules_path = rules_path.replace("/", System.getProperty("file.separator"));
//		File f = new File(rules_path);
//		// SharedKnowledgePlane.getInstance(this.pacID).addRulesFile(f,
//		// ResourceType.DRL);
//		createAgents();
	}

	
	   /**
     * 1. Gets 'application started' events from the message queue
     */
    private void retrieveVmEvents() {
    	try{
    	
    	// Getting JMS connection from the server

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.getProperty(ACTIVEMQ_URL));
        Connection connection = connectionFactory.createConnection();

        // need to setClientID value, any string value you wish
        connection.setClientID("IaaS SLAM Application Event Listener");

        
        connection.start();
        
        Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic(properties.getProperty(DEPLOYED_VMS_QUEUE));

        //need to use createDurableSubscriber() method instead of createConsumer() for topic
        // MessageConsumer consumer = session.createConsumer(topic);
        MessageConsumer consumer = session.createDurableSubscriber(topic,
        		properties.getProperty(ACTIVEMQ_CHANNEL));

        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        logger.info("ACTIVEMQ: Received message in the VM deployed topic: "
                                + textMessage.getText());
                        
                        /*
                         * TODO
                         * what I retrieve from VMM messages? vmId and ovfId?
                         */
                        
                        logger.info("Retrieving VM Details from the VM Manager...");
                        String slaId = retrieveVmDetails(null,null);
                        
                        logger.info("Creating an instance of Iaas Violation Checker...");
                        new Thread(new IaasViolationChecker(properties, "infrastructure-monitor.monitoring.measurement", textMessage.getText(), textMessage.getText(), slaId)).start();
                        
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
     * 2. Retrieve vm details from the vm manager whenever a 
     * "vm starting" event is catched
     */
    private String retrieveVmDetails(String vmId, String ovfId) {
    	VmManagerClient vmm = new VmManagerClient(properties.getProperty(VMMANAGER_URL));
//        System.out.println(vmm.getVm("55a440c1-14ff-446d-9410-a2dce763085c"));

    	
    	VmDeployed vm = vmm.getVm(vmId);
    	String slaId = vm.getSlaId();
    	System.out.println("SLA ID: "+slaId);
    	
    	return slaId;
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

	/**
	 * Method that will be invoked when an agent sends an event to this PAC.
	 * 
	 * @param event
	 *            event sent by an agent to this PAC.
	 */
	public void notifyEvent(Event event) {
		logger.debug("Ascetic PAC::notifyEvent: " + event.getEventContent().toString());
		EventType type = event.getType();
		logger.debug("Ascetic PAC::notifyEvent " + type.toString());
		logger.debug("ascetic PAC::notifyEvent: " + event.getEventContent().toString());

	}

	public static String getConfigurationFileImpl() {
		return configurationFileImpl;
	}

	public static void main(String[] args) {
		ProvisioningAdjustmentImpl pai = new ProvisioningAdjustmentImpl();
		pai.retrieveVmDetails("", "");
	}
}
