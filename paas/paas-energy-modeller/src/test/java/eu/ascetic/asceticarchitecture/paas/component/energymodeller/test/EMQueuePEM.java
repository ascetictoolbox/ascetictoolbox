package eu.ascetic.asceticarchitecture.paas.component.energymodeller.test;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.ApplicationRegistry;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.common.data.ibatis.DataConsumptionHandler;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.EnergyModellerQueueServiceManager;
import eu.ascetic.asceticarchitecture.paas.component.energymodeller.internal.queue.client.AmqpClient;


public class EMQueuePEM {
	
	private static AmqpClient paasQm;
	private static AmqpClient iaasQm;
	private static ApplicationRegistry registry;
	private static DataConsumptionHandler dataCollectorHandler;
	static EnergyModellerQueueServiceManager queueManager;
	

	 

	
	@BeforeClass
	public static void setup() {
		paasQm = new AmqpClient();
		iaasQm =  new AmqpClient();
		try {
			paasQm.setup("192.168.3.16:5673", "guest", "guest", "PEMENERGY");
			iaasQm.setup("192.168.3.17:5673", "guest", "guest");
			registry = ApplicationRegistry.getRegistry("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
			dataCollectorHandler = DataConsumptionHandler.getHandler("com.mysql.jdbc.Driver","jdbc:mysql://10.15.5.55:3306/ascetic_paas_em","root","root");
			queueManager = new EnergyModellerQueueServiceManager(iaasQm,paasQm,registry,dataCollectorHandler);
			queueManager.createTwoLayersConsumers("APPLICATION.*.DEPLOYMENT.*.VM.*.*","vm.*.item.power");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void sendMessagePrediction() throws Exception {
		
		
		while (true){
			
		}
		

	}
	

}
