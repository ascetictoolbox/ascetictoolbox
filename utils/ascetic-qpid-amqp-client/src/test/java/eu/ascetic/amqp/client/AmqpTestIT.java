package eu.ascetic.amqp.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AmqpTestIT {

	@Test
	public void testWithRealAmpq10server() throws Exception {

		String serverAddress = "localhost:5672";
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
		assertEquals("testX", listener.getMessage());
		
		receiver.close();
		producer.close();
	}
	
	@Test
	public void testWithRealAmqpServer2() throws Exception {
		String serverAddress = "localhost:5672";
		String user = "guest";
		String password = "guest";
		String queueOrTopicName = "application.232.deployment.222";
		boolean topic = true;
		
		AmqpMessageProducer producer = new AmqpMessageProducer(serverAddress, user, password,  queueOrTopicName, topic);
		
		producer.sendMessage("Message 111");
		
		Thread.sleep(2000l);
		
		producer.sendMessage("Message 222");
		
		Thread.sleep(3000l);
		
		producer.sendMessage("Message 333");

		producer.close();
	}
}
