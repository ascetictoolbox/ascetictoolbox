package es.bsc.amon.amqp.test;

import junit.framework.TestCase;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


public class PredictionAMQPTest
    extends TestCase
{

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
     * Rigourous Test :-)
     */
    public void testCall() throws Exception {

		Properties p = new Properties();
		p.load(getClass().getResourceAsStream("/jndi2.properties"));
		final Context context = new InitialContext(p);

		TopicConnectionFactory connectionFactory
				= (TopicConnectionFactory) context.lookup("asceticpaas");





		//Queue sendQueue = (Queue) context.lookup("appmon");
		Topic topic = (Topic) context.lookup("prediction");


		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			/*
			try {
				connection.close();
			} catch (JMSException e) {
				e.printStackTrace();
			}
			*/
			try {
				context.close();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}));
		final TopicConnection connection = connectionFactory.createTopicConnection();
		connection.start();
		TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		TopicSubscriber clientTopic = session.createSubscriber(topic);

		while(true) {
			try {
				TextMessage tm = (TextMessage) clientTopic.receive(5000);
//				JmsBytesMessage tm = (JmsBytesMessage) clientTopic.receive(5000);
//				System.out.println("tm.getJMSType() = " + tm.readUTF());
				if(tm != null) System.out.println("received message: " + tm.getText());
			} catch(JMSException e) {
				Thread.sleep(3000);
				System.err.println(e.getMessage());
			}
		}


	}
}
