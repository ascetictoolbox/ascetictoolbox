/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.test.iaas.vmm.base;

import es.bsc.vmmclient.models.*;
import es.bsc.vmmclient.vmm.VmManagerClient;
import eu.ascetic.test.conf.VMMConf;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * 
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class VmmTestBase extends TestCase {
    private static final Logger logger = Logger.getLogger("VmmTestBase");
    
    private int iterations;
    private static final int MAX_ITERATIONS = 100;
    public VmManagerClient vmm;
    public String vmId = null;
    public String environment = null;
    public String mqURL = null;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        vmm = new VmManagerClient(VMMConf.vmManagerURL);
        environment = VMMConf.environment;
        mqURL = VMMConf.activeMqUrl;
        iterations = 0;
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(vmId != null){
            //Destroy
            logger.info("Destroying VM " + vmId);
            vmm.destroyVm(vmId);
            
            vmId = null;
        }
    }
    
    public boolean loopIsAlive() throws Exception{
        if(iterations++ > MAX_ITERATIONS){
            throw new Exception("Timeout reached! This loop cannot do more than " + 
                MAX_ITERATIONS + " iterations.");
        }
        
        return true;
    }
    
    public void waitForIt(Integer seconds, String message){
        try {
            logger.info(message + " [" + seconds + " seconds]");
            Thread.sleep(seconds*1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void sendFakeSLAMessage(String vmId, String slaId, String ovfId, String slaAgreementTerm, String guaranteedValue, String usageValue) throws Exception {
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String now = dt.format(new Date()).replace(" ", "T");
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(mqURL);
		Connection c = connectionFactory.createConnection();
		Session qs = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
		TextMessage tm = qs.createTextMessage(
            "<ViolationMessage vmId=\"" + vmId + "\" ovfId=\"" + ovfId + "\">\n" +
            "  <time>" + now + "</time>\n" +
            "  <value id=\"usage\">" + usageValue + "</value>\n" +
            "  <alert>\n" +
            "    <type>violation</type>\n" +
            "    <slaUUID>" + slaId + "</slaUUID>\n" +
            "    <slaAgreementTerm>" + slaAgreementTerm + "</slaAgreementTerm>\n" +
            "    <slaGuaranteedState>\n" +
            "      <guaranteedId>" + slaAgreementTerm + "</guaranteedId>\n" +
            "      <operator>EQUAL</operator>\n" +
            "      <guaranteedValue>" + guaranteedValue + "</guaranteedValue>\n" +
            "      <this_-1 reference=\"../..\"/>\n" +
            "    </slaGuaranteedState>\n" +
            "    <outer-class reference=\"../..\"/>\n" +
            "  </alert>\n" +
            "</ViolationMessage>"
        );
        
		Destination q = qs.createQueue(String.format(VIOLATION_QUEUE_NAME, slaId, vmId));
		MessageProducer mp = qs.createProducer(q);
		mp.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		mp.send(tm);
		qs.close();
		c.close();
	}
    
    private static final String VIOLATION_QUEUE_NAME = "iaas-slam.monitoring.%s.%s.violationNotified";
}
