package es.bsc.vmm.ascetic.mq;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Ignore;

import javax.jms.*;

/**
 * Created by mmacias on 3/12/15.
 */
@Ignore
public class TestMQQueueSendMsg extends TestCase {
	@Ignore
	public void testSendSLAViolation() throws Exception {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://iaas-test:61616");
		Connection c = connectionFactory.createConnection();
		Session qs = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		TextMessage tm = qs.createTextMessage("Quepasa coleguita");
		Destination q = qs.createQueue("iaas-slam.monitoring.641bfdc1-528b-494d-9fe2-aa023ce2ec51.4ae708c9-aca0-404c-b639-6454a91ba147.violationNotified");
		MessageProducer mp = qs.createProducer(q);
		mp.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		mp.send(tm);
		qs.close();
		c.close();
	}
}
