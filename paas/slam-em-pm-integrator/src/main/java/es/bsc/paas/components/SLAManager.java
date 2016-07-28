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

	private static final String QUEUE_TEMPLATE = "application-monitor.monitoring.%s.%s.estimation";

	private Logger log = LoggerFactory.getLogger(SLAManager.class);

	public void reportEstimation(String applicationId, String deploymentId, long referredtimestamp, double energyEstimation, double priceEstimation) {
		String queueName = String.format(QUEUE_TEMPLATE,applicationId,deploymentId);

		log.debug("Queue name: " + queueName);
		final String jsonDocument = new StringBuilder("{\"ApplicationId\":\"")
				.append(applicationId)
				.append("\",\"DeploymentId\":\"")
				.append(deploymentId)
				.append("\",\"Timestamp\":")
				.append(System.currentTimeMillis())
				.append(",\"data\":{\"energyEstimation\":")
				.append(energyEstimation)
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
}
