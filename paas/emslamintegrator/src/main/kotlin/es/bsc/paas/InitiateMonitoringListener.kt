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
        logger.info("STARTING")
    }


    @JmsListener(destination = "testQueue")
    fun processQueueMessage(content:String) {
        logger.info("****** RECEIVED CONTENT FROM QUEUE")
        logger.info(content)
    }


    @JmsListener(destination = "testTopic")
    fun processTopicMessage(content:String) {
        logger.info("****** RECEIVED CONTENT FROM TOPIC")
        logger.info(content)
    }
}