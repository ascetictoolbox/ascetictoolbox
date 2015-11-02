/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.activemq.actuator;

import eu.ascetic.paas.applicationmanager.amqp.model.ApplicationManagerMessage;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.model.converter.ModelConverter;
import eu.ascetic.paas.self.adaptation.manager.ActuatorInvoker;
import eu.ascetic.paas.self.adaptation.manager.activemq.ActiveMQBase;
import eu.ascetic.paas.self.adaptation.manager.rules.datatypes.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This requests adaptation actions to be performed in order to perform
 * adaptation.
 *
 * @author Richard Kavanagh
 */
public class ActionRequester extends ActiveMQBase implements Runnable, ActuatorInvoker {

    private final MessageProducer producer;
    private final Destination messageQueue;
    private final LinkedBlockingDeque<Response> queue = new LinkedBlockingDeque<>();
    private boolean stop = false;
    private static String queueName = "";
    private static final String CONFIG_FILE = "paas-self-adaptation-manager.properties";

    /**
     * This creates a new action requester.
     *
     * @throws JMSException
     * @throws NamingException
     */
    public ActionRequester() throws JMSException, NamingException {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            queueName = config.getString("paas.self.adaptation.manager.app.manager.queue.name", queueName);
            config.setProperty("paas.self.adaptation.manager.app.manager.queue.name", queueName);
        } catch (ConfigurationException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.INFO, "Error loading the configuration of the PaaS Self adaptation manager", ex);
        }
        connection = getConnection();
        // Create a Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // Create the destination (Topic or Queue)
        messageQueue = getMessageQueue(queueName);
        producer = getMessageProducer(session, messageQueue);
    }

    /**
     * This sends a message to the topic of the message queue of the actuator
     * that performs work on behalf of the self-adaptation manager.
     *
     * @param message text message to be sent.
     * @throws JMSException
     */
    public void sendMessage(String message) throws JMSException {
        TextMessage testMessage = session.createTextMessage(message);
        producer.send(testMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
    }

    /**
     * This is the main run method of the action requester. It waits and submits
     * actions to the self adaptation manager's actuators.
     */
    @Override
    public void run() {
        while (!stop || !queue.isEmpty()) {
            try {
                Response currentItem = queue.poll(30, TimeUnit.SECONDS);
                if (currentItem != null) {
                    ArrayList<Response> actions = new ArrayList<>();
                    actions.add(currentItem);
                    queue.drainTo(actions);
                    for (Response action : actions) {
                        launchAction(action);
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        close();
    }

    /**
     * This executes a given action for a response that has been placed in the
     * actuator's queue for deployment.
     *
     * @param response The response object to launch the action for
     */
    private void launchAction(Response response) {
        switch (response.getActionType()) {
            case ADD_VM:
                addVM(response.getApplicationId(), response.getDeploymentId(), response.getAdaptationDetails());
                break;
            case REMOVE_VM:
                deleteVM(response.getApplicationId(), response.getDeploymentId(), response.getVmId());
                break;
        }
        response.setPerformed(true);
    }

    @Override
    public List<String> getVmTypesAvailableToAdd(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMax() > 0 && getVmCountOfGivenType(vms, vm.getOvfId()) < vm.getNumberVMsMax()) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }

    @Override
    public List<String> getVmTypesAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<String> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0 && getVmCountOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()) {
                answer.add(vm.getOvfId());
            }
        }
        return answer;
    }

    @Override
    public List<Integer> getVmIdsAvailableToRemove(String applicationId, String deploymentId) {
        ArrayList<Integer> answer = new ArrayList<>();
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            if (vm.getNumberVMsMin() > 0 && getVmCountOfGivenType(vms, vm.getOvfId()) > vm.getNumberVMsMin()) {
                answer.add(vm.getId());
            }
        }
        return answer;
    }

    @Override
    public int getVmCountOfGivenType(List<VM> vms, String type) {
        int answer = 0;
        for (VM vm : vms) {
            if (vm.getOvfId().equals(type)) {
                answer = answer + 1;
            }
        }
        return answer;
    }

    /**
     * This gets a VM given its application, deployment and VM ids.
     *
     * @param applicationId The application ID
     * @param deploymentId The deployment ID
     * @return The VM given the id values specified.
     */
    public List<VM> getVMs(String applicationId, String deploymentId) {
        //TODO - GET LIST OF VMs from ACTIVE MQ
        return null;
    }

    @Override
    public void addVM(String applicationId, String deploymentId, String ovfId) {
        try {
            // Create a messages
            String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
            sendMessage(text);
        } catch (JMSException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void deleteVM(String application, String deployment, String vmID) {
        try {
            // Create a messages
            ApplicationManagerMessage message = new ApplicationManagerMessage();
            message.setApplicationId(application);
            message.setDeploymentId(deployment);
            //TODO, complete the code here!!!!
            String jsonMessage = ModelConverter.applicationManagerMessageToJSON(message);
            sendMessage(jsonMessage);
        } catch (JMSException ex) {
            Logger.getLogger(ActionRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This deletes all VMs of an application
     *
     * @param applicationId The application the VM is part of
     * @param deploymentId The id of the deployment instance of the VM
     */
    @Override
    public void hardShutdown(String applicationId, String deploymentId) {
        ApplicationManagerMessage message = new ApplicationManagerMessage();
        message.setApplicationId(applicationId);
        message.setDeploymentId(deploymentId);     
        List<VM> vms = getVMs(applicationId, deploymentId);
        for (VM vm : vms) {
            deleteVM(applicationId, deploymentId, vm.getId() + "");
        }
    }

    @Override
    public void actuate(Response response) {
        queue.add(response);
    }

}
