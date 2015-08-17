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
package eu.ascetic.paas.self.adaptation.manager.activemq.listener;

import eu.ascetic.paas.self.adaptation.manager.rules.ThresholdEventAssessor;
import javax.jms.JMSException;
import javax.naming.NamingException;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is the unit test for the SLA manager
 * @author Richard Kavanagh
 */
public class SlaManagerListenerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of run method, of class SlaManagerListener.
     */
    @Test
    public void testRun() {
        try { //"10.4.0.16:5672" "http://192.168.3.16:5673", //vm:localhost:5673
            SLAManagerMessageGenerator generator = new SLAManagerMessageGenerator(
                    "guest", "guest", "192.168.3.16:5673",
                    "paas-slam.monitoring.f28d4719-5f98-4c87-9365-6be602da9a4a.DavidgpTestApp.violationNotified");
            Thread genThread = new Thread(generator);
            genThread.setDaemon(true);
            genThread.start();
            System.out.println("run"); //"password"   //"192.168.3.16:5673"
                SlaManagerListener instance = new SlaManagerListener(
                    "guest", "guest", "192.168.3.16:5673",
                    "paas-slam.monitoring.f28d4719-5f98-4c87-9365-6be602da9a4a.DavidgpTestApp.violationNotified");
            instance.setEventAssessor(new ThresholdEventAssessor());
                Thread instThread = new Thread(instance);
            instThread.start();
            //An event should arrive in this period of time.
            instance.printQueueAndTopicInformation();
            Thread.sleep(60000);
            instance.stopListening();
        }catch (InterruptedException ex) {
            ex.printStackTrace();
            fail("An interrupted exception was thrown");
        } catch (JMSException ex) {
            ex.printStackTrace();
            fail("An JMS exception was thrown");
        } catch (NamingException ex) {
            ex.printStackTrace();
            fail("An naming exception was thrown");
        }
    }

//    /**
//     * Test of setEventAssessor method, of class SlaManagerListener.
//     */
//    @Test
//    public void testSetEventAssessor() {
//        System.out.println("setEventAssessor");
//        try {
//            EventAssessor assessor = null;
//            SlaManagerListener instance = new SlaManagerListener();
//            instance.setEventAssessor(assessor);
//        } catch (JMSException ex) {
//            fail("An exception was thrown");
//        } catch (NamingException ex) {
//            fail("An exception was thrown");
//        }
//    }
//    
//    /**
//     * Test of getEventAssessor method, of class SlaManagerListener.
//     */
//    @Test
//    public void testGetEventAssessor() {
//        System.out.println("getEventAssessor");
//        try {
//            SlaManagerListener instance = new SlaManagerListener();
//            EventAssessor expResult = null;
//            EventAssessor result = instance.getEventAssessor();
//            assertEquals(expResult, result);
//        } catch (JMSException ex) {
//            fail("An exception was thrown");
//        } catch (NamingException ex) {
//            fail("An exception was thrown");
//        }
//    }
//
//    /**
//     * Test of stopListening method, of class SlaManagerListener.
//     */
//    @Test
//    public void testStopListening() {
//        System.out.println("stopListening");
//        try {
//            SlaManagerListener instance = new SlaManagerListener();
//            instance.stopListening();
//        } catch (JMSException ex) {
//            fail("An exception was thrown");
//        } catch (NamingException ex) {
//            fail("An exception was thrown");
//        }
//    }

}
