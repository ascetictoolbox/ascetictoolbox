package es.bsc.paas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
public class InitiateMonitoringListener {


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
    }
}