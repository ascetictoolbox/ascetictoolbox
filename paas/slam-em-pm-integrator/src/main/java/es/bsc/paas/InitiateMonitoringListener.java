package es.bsc.paas;

import es.bsc.paas.components.PaasEnergyModeller;
import es.bsc.paas.model.InitiateMonitoringCommand;
import es.bsc.paas.scheduling.SchedulingReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This application just listens for the "initiateMonitoring" command in the ActiveMQ topic that is
 * specified by the "topic.name" property (default value: appmonitoring)
 *
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class InitiateMonitoringListener {

	@Autowired
	SchedulingReporter schedulingReporter;

    private Logger logger = LoggerFactory.getLogger(InitiateMonitoringListener.class);
    public InitiateMonitoringListener() {
        logger.debug("Starting InitiateMonitoringListener");
    }


	/**
	 * This method is triggered each time the SLA Manager submits the "initiateMonitoring" command to the
	 * appmonitoring queue
	 * @param content the JSON document of the "initiateMonitoring" command
	 */
	@JmsListener(destination = "${topic.name}")
    private void processTopicMessage(String content) {

		/* TODO: now it assumes all the messages are "initiateMonitoring". It would be interesting
		   to create other commands, such as "stopMonitoring" */
        logger.debug("Received from Topic: " + content);
        try {
            schedulingReporter.onInitiateMonitoringCommandInfo(InitiateMonitoringCommand.fromJson(content));
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
        }
    }


}