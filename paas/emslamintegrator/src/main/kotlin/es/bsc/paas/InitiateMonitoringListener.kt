package es.bsc.paas

import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component

/**
 * @author Mario Macias (http://github.com/mariomac)
 */
@Component
class InitiateMonitoringListener {
    val logger = LoggerFactory.getLogger(InitiateMonitoringListener::class.java)
    constructor() {
        logger.debug("Starting InitiateMonitoringListener")
    }


//    @JmsListener(destination = "appmon")
//    fun processQueueMessage(content:String) {
//        logger.debug("Received from Queue: $content")
//
//    }


    @JmsListener(destination = "appmon")
    fun processTopicMessage(content:String) {
        logger.debug("Received from Topic: $content")
    }
}