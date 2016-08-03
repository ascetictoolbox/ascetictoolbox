package es.bsc.paas.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Client class to submit price and energy estimations to the SLAM
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class SLAManager {

    @Autowired
    ApplicationContext context;

    private static final String QUEUE_ESTIMATIONS = "application-monitor.monitoring.%s.%s.estimation";
    private static final String QUEUE_MEASUREMENTS = "application-monitor.monitoring.%s.measurement";

    private Logger log = LoggerFactory.getLogger(SLAManager.class);

    public void reportEstimation(String applicationId, String deploymentId, long referredtimestamp, 
        double energyEstimation, double powerEstimation, double priceEstimation) {
        
        String queueName = String.format(QUEUE_ESTIMATIONS, applicationId, deploymentId);
        log.debug("Queue name: " + queueName);
        final String jsonDocument = new StringBuilder("{\"ApplicationId\":\"")
            .append(applicationId)
            .append("\",\"DeploymentId\":\"")
            .append(deploymentId)
            .append("\",\"Timestamp\":")
            .append(System.currentTimeMillis())
            .append(",\"data\":{\"energyEstimation\":")
            .append(energyEstimation)
            .append(",\"powerEstimation\":")
            .append(powerEstimation)
            .append(",\"priceEstimation\":")
            .append(priceEstimation)
            .append("}}").toString();

        log.debug(jsonDocument);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(jsonDocument);
            }
        };
        
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.send(queueName, messageCreator);
    }
    
    public void reportMeasurement(String applicationId, String deploymentId, long referredtimestamp, 
        double energyConsumption, double powerConsumption) {
        
        String queueName = String.format(QUEUE_MEASUREMENTS, applicationId);
        log.debug("Queue name: " + queueName);
        final String jsonDocument = new StringBuilder("{\"ApplicationId\":\"")
            .append(applicationId)
            .append("\",\"DeploymentId\":\"")
            .append(deploymentId)
            .append("\",\"Timestamp\":")
            .append(System.currentTimeMillis())
            .append(",\"data\":{\"energyConsumption\":")
            .append(energyConsumption)
            .append(",\"powerConsumption\":")
            .append(powerConsumption)
            .append("}}").toString();

        log.debug(jsonDocument);
        MessageCreator messageCreator = new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(jsonDocument);
            }
        };
        
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        jmsTemplate.send(queueName, messageCreator);
    }
}
