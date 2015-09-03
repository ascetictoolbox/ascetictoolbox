package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import java.util.Date;

import javax.jms.JMSException;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;


public class EMQueuePEM {
	
	private static AmqpClient qm;
	private static ApplicationRegistry registry;
	private static DataConsumptionHandler dataCollectorHandler;
	static EnergyModellerQueueServiceManager queueManager;
	

	 

	
	@BeforeClass
	public static void setup() {
		qm = new AmqpClient();
		try {
			qm.setup("10.15.5.55:32772", "admin", "admin", "PEMENERGY");
			registry = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
			dataCollectorHandler = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
			queueManager = new EnergyModellerQueueServiceManager(qm,registry,dataCollectorHandler);
			queueManager.createConsumers("APPLICATION.*.DEPLOYMENT.*.VM.*.*","vm.*.item.* ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void sendMessagePrediction() throws Exception {
		
		Date data = new Date();
		AmqpClient am = new AmqpClient();
		am.setup("10.15.5.55:32772", "admin", "admin", "");
		am.sendMessageTopic("APPLICATION.1.DEPLOYMENT.2.VM.3.DEPLOYED", data.toGMTString());
		am.sendMessageTopic("APPLICATION.1.DEPLOYMENT.2.VM.4.TERMINATED", data.toGMTString());
		am.sendMessageTopic("APPLICATION.1.DEPLOYMENT.2.VM.4.COMMAND", data.toGMTString());
		am.sendMessageTopic("vm.3.item.energy", "{}");
		
		while (true){
			
		}
		
//		try {
//			qm.destroy();
//		} catch (JMSException e) {
//			e.printStackTrace();
//		}
	}
	

}
