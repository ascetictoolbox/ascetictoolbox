package eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.queue.client;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpBasicListener;
import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.asceticarchitecture.paas.paaspricingmodeller.PaaSPricingModeller;

/*To be called when we want to create a new listener to an existing queue or to create a producer*/

public class QueueInitializator {
	

    Timer timer = new Timer (true);
    long delay = 20;

    private final static Logger LOGGER = Logger.getLogger(QueueInitializator.class.getName());
    
    public AmqpMessageProducer InitializeProducerQueue(String queueName, String usern, String password, String topicName, Boolean topic) throws Exception{
    	AmqpMessageProducer pricingqueue = new AmqpMessageProducer(queueName, usern, password, topicName, topic);
		LOGGER.info("PaaS Pricing Queue initialized");
		return  pricingqueue;

    }
	
	public void InitializeRecieverQueue(String queueName, String usern, String password, String topicName, Boolean topic, PaaSPricingModeller provider) throws Exception{
		
		try{
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(queueName, usern, password, topicName, topic);
		AmqpBasicListener listener = new AmqpBasicListener();
	    receiver.setMessageConsumer(listener);
		timer.scheduleAtFixedRate(new PaaSPricingMessageHandler(listener, provider), TimeUnit.SECONDS.toMillis(delay), 1000);
		}
		catch (NullPointerException ex){
			LOGGER.info("Could not create queue");
		}
	}
	
}