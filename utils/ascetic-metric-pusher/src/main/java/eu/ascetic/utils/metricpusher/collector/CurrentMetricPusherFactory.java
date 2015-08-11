package eu.ascetic.utils.metricpusher.collector;

import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary;
import eu.ascetic.utils.metricpusher.amqp.AmqpProducer;
import eu.ascetic.utils.metricpusher.conf.Configuration;


public class CurrentMetricPusherFactory extends Thread {

	private static Logger logger = Logger.getLogger(CurrentMetricPusherFactory.class);
	private ZabbixClient client;
	
	public CurrentMetricPusherFactory(){
		client = new ZabbixClient();
	}
	
	@SuppressWarnings("unchecked")  
	public void run()  
	{  
		while (true){
			try {	

				List<Host> vms = client.getVms();
//				List<Host> vms = client.getAllHosts();
				for (Host vm : vms){
					publishCurrentEnergy(vm.getHost());
					publishCurrentPower(vm.getHost());
				}
				
				//wait 60 seconds to push more data to Communication middleware
				Thread.sleep(Long.parseLong(Configuration.publishFrequency));
			}
			catch (Exception e){
				logger.error(e.getMessage());
			}
		}
	}  
	

	private void publishCurrentPower(String vmId) {
		String item = "power";
		Item i = client.getItemByKeyFromHost(item, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "W";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
//			System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
//					" " + units);
			AmqpProducer.sendCurrentValueForItemInHostMessage(vmId, item, value, units, i.getLastClock());
		}
		else {
			logger.info("No power value for VM " + vmId);
		}
	}

	
	private void publishCurrentEnergy(String vmId) {
		String item = "energy";
		Item i = client.getItemByKeyFromHost(item, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "Wh";
			if (value>1000){
				value = value/1000;
				value = Dictionary.round(value, 2);
				units = "KWh";
			}
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + value + " " + units);
//			System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + value + " " + units);
			AmqpProducer.sendCurrentValueForItemInHostMessage(vmId, item, value, units, i.getLastClock());
		}
		else {
			logger.info("No energy value for VM " + vmId);
		}
	}	

}
