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
