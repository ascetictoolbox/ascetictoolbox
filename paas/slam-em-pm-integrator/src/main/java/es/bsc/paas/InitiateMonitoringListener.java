package es.bsc.paas;

import es.bsc.paas.components.PaasEnergyModeller;
import es.bsc.paas.scheduling.SchedulingReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class InitiateMonitoringListener {

	@Autowired
	PaasEnergyModeller energyModeller;

	@Autowired
	SchedulingReporter schedulingReporter;

    private Logger logger = LoggerFactory.getLogger(InitiateMonitoringListener.class);
    public InitiateMonitoringListener() {
        logger.debug("Starting InitiateMonitoringListener");
    }



    @JmsListener(destination = "${topic.name}")
    private void processTopicMessage(String content) {
        logger.debug("Received from Topic: " + content);
		logger.debug("hello EM: " + energyModeller.hello());
		schedulingReporter.jarls.add(content);
    }


}