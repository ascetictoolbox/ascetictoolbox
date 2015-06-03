package eu.ascetic.amqp.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AmqpTestIT {

	@Test
	public void testWithRealAmpq10server() throws Exception {

		String serverAddress = "localhost:5673";
		String user = "guest";
		String password = "guest";
		String queueOrTopicName = "myTopic";
		boolean topic = true;
		
		AmqpMessageReceiver receiver = new AmqpMessageReceiver(serverAddress, user, password,  queueOrTopicName, topic);
		AmqpBasicListener listener = new AmqpBasicListener();
		receiver.setMessageConsumer(listener);
		AmqpMessageProducer producer = new AmqpMessageProducer(serverAddress, user, password,  queueOrTopicName, topic);
		
		producer.sendMessage("testX");
		
		Thread.sleep(1000l);
		
		assertEquals("myTopic", listener.getDestination());
		assertEquals("testX", listener.getDestination());
		
		receiver.close();
		producer.close();
	}
}
