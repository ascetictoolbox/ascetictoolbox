/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.activemq;

import eu.ascetic.amqp.client.AmqpExceptionListener;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * The test class for the ACTIVE MQ Base.
 *
 * @author Richard Kavanagh
 */
public class ActiveMQBaseTest {

    public ActiveMQBaseTest() {
    }

    /**
     * Test of setAmqpExceptionListener method, of class ActiveMQBase.
     */
    @Test
    public void testSetAmqpExceptionListener() {
        try {
            System.out.println("setAmqpExceptionListener");
            AmqpExceptionListener amqpExceptionListener = null;
            ActiveMQBase instance = new ActiveMQBaseImpl();
            instance.setAmqpExceptionListener(amqpExceptionListener);
        } catch (Exception ex) {
            Logger.getLogger(ActiveMQBaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    /**
//     * Test of getConnection method, of class ActiveMQBase.
//     */
//    @Test
//    public void testGetConnection() throws Exception {
//        System.out.println("getConnection");
//        ActiveMQBase instance = new ActiveMQBaseImpl();
//        Connection result = instance.getConnection();
//        assert(result != null);
//    }

    /**
     * Test of getMessageProducer method, of class ActiveMQBase.
     */
    @Test
    public void testGetMessageProducer() throws Exception {
        System.out.println("getMessageProducer");
        Session session = null;
        Destination queue = null;
        ActiveMQBase instance = new ActiveMQBaseImpl();
        MessageProducer expResult = null;
        MessageProducer result = instance.getMessageProducer(session, queue);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMessageQueue method, of class ActiveMQBase.
     */
    @Test
    public void testGetMessageQueue() throws Exception {
        System.out.println("getMessageQueue");
        String queue = "";
        ActiveMQBase instance = new ActiveMQBaseImpl();
        Destination result = instance.getMessageQueue(queue);
        assert(result == null);
    }

    /**
     * Test of getTopic method, of class ActiveMQBase.
     */
    @Test
    public void testGetTopic() throws Exception {
        System.out.println("getTopic");
        String topic = "";
        ActiveMQBase instance = new ActiveMQBaseImpl();
        Destination result = instance.getTopic(topic);
        assert(result == null);
    }

    /**
     * Test of getQueues method, of class ActiveMQBase.
     */
    @Test
    public void testGetQueues() {
        try {
            System.out.println("getQueues");
            ActiveMQBase instance = new ActiveMQBaseImpl();
            Set<ActiveMQQueue> result = instance.getQueues();
            assert(result != null);
        } catch (Exception ex) {
            Logger.getLogger(ActiveMQBaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getTopics method, of class ActiveMQBase.
     */
    @Test
    public void testGetTopics() {
        try {
            System.out.println("getTopics");
            ActiveMQBase instance = new ActiveMQBaseImpl();
            Set<ActiveMQTopic> result = instance.getTopics();
            assert(result != null);
        } catch (Exception ex) {
            Logger.getLogger(ActiveMQBaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of printQueueAndTopicInformation method, of class ActiveMQBase.
     */
    @Test
    public void testPrintQueueAndTopicInformation() {
        try {
            System.out.println("printQueueAndTopicInformation");
            ActiveMQBase instance = new ActiveMQBaseImpl();
            instance.printQueueAndTopicInformation();
        } catch (Exception ex) {
            Logger.getLogger(ActiveMQBaseTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of close method, of class ActiveMQBase.
     */
    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        ActiveMQBase instance = new ActiveMQBaseImpl();
        instance.close();
    }

    public class ActiveMQBaseImpl extends ActiveMQBase {

        public ActiveMQBaseImpl() throws Exception {
            super();
        }
    }

}
