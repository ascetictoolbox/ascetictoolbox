package es.bsc.paas;

import es.bsc.paas.modellers.PaasEnergyModeller;
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

    private Logger logger = LoggerFactory.getLogger(InitiateMonitoringListener.class);
    public InitiateMonitoringListener() {
        logger.debug("Starting InitiateMonitoringListener");
    }


//    @JmsListener(destination = "appmon")
//    fun processQueueMessage(content:String) {
//        logger.debug("Received from Queue: $content")
//
//    }


    @JmsListener(destination = "${topic.name}")
    private void processTopicMessage(String content) {
        logger.debug("Received from Topic: " + content);
		logger.debug("hello EM: " + energyModeller.hello());
    }


}