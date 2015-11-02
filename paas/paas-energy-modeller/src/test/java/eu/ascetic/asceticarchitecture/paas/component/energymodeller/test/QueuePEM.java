/**
   Copyright 2014-2015 Hewlett-Packard Development Company, L.P.  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.datatype.messages.GenericEnergyMessage.Unit;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.queue.MessageParserUtility;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;


public class QueuePEM {
	
	private static AmqpClient qm;
	
	@BeforeClass
	public static void setup() {
		qm = new AmqpClient();
		try {
			qm.setup("10.15.5.55:32772", "admin", "admin", "PEMENERGY");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void sendMessagePrediction() {
		GenericEnergyMessage message = new GenericEnergyMessage();
		message.setApplicationid("testapp");
		message.setEventid("noevent");
		List<String> vms = new Vector<String>();
		vms.add("vm1");
		vms.add("vm2");
		message.setVms(vms);
		message.setUnit(Unit.WATT);
		message.setProvider("theprovider");
		message.setValue(50.4);
		Date data = new Date();
		message.setGenerattiontimestamp(data.toGMTString());
		message.setReferredtimestamp(data.toGMTString());
		qm.sendMessage("prediction",MessageParserUtility.buildStringMessage(message));
		qm.sendMessage("measurement",MessageParserUtility.buildStringMessage(message));
		try {
			qm.destroy();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
