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



    @JmsListener(destination = "${topic.name}")
    private void processTopicMessage(String content) {
        logger.debug("Received from Topic: " + content);
        try {
            schedulingReporter.onInitiateMonitoringCommandInfo(InitiateMonitoringCommand.fromJson(content));
        } catch (IOException e) {
            logger.warn(e.getMessage(),e);
        }
    }


}