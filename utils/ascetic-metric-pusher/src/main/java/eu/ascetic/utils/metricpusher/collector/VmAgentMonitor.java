package eu.ascetic.utils.metricpusher.collector;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.utils.metricpusher.amqp.AmqpProducer;
import eu.ascetic.utils.metricpusher.conf.Configuration;
import eu.ascetic.utils.metricpusher.conf.Dictionary;
import eu.ascetic.utils.metricpusher.pusher.MetricPusher;

public class VmAgentMonitor extends Thread {

	private static Logger logger = Logger.getLogger(VmAgentMonitor.class);
	private String hostname;
	private ZabbixClient client;
	
	public VmAgentMonitor(String hostname){
		this.hostname = hostname;
		client = new ZabbixClient();
	}
	
	@SuppressWarnings("unchecked")  
	public void run()  
	{  if (MetricPusher.SHOW_DEBUG_TRACES){
		System.out.println("Launching VmAgentMonitor for " + hostname + "...");
	}
		while (true){
			try {
//				publishCurrentEnergy(hostname);
				publishCurrentPower(hostname);
				publishCurrentCpu(hostname);
				publishCurrentMemory(hostname);
				publishCurrentTransmittedBytes(hostname);
				publishCurrentReceivedBytes(hostname);
				publishCurrentIdlePower(hostname);
				publishCurrentNetPower(hostname);
				if (MetricPusher.SHOW_DEBUG_TRACES){
					System.out.println("********************************************************");
				}
				//wait 40 seconds to push more data to Communication middleware
				Thread.sleep(Long.parseLong(Configuration.publishFrequency));
			}
			catch (Exception e){
				logger.error(e.getMessage());
			}
		}		
	}
	
	
	private void publishCurrentReceivedBytes(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_RX_BYTES_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			long value =  Long.parseLong(i.getLastValue());
			String units = "bytes";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
						" " + units);
			}
			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, Dictionary.ITEM_RX_BYTES_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_RX_BYTES_KEY + " value for VM " + vmId);
		}	
	}

	private void publishCurrentTransmittedBytes(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_TX_BYTES_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			long value =  Long.parseLong(i.getLastValue());
			String units = "bytes";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
						" " + units);
			}
			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, Dictionary.ITEM_TX_BYTES_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_TX_BYTES_KEY + " value for VM " + vmId);
		}	
	}

	private void publishCurrentMemory(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_MEMORY_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			long value =  Long.parseLong(i.getLastValue());
			String units = "bytes";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
			}
			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, Dictionary.ITEM_MEMORY_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_MEMORY_KEY + " value for VM " + vmId);
		}	
	}

	private void publishCurrentCpu(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_CPU_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "Mhz";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			}
			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_CPU_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_CPU_KEY + " value for VM " + vmId);
		}
		
	}

	private void publishCurrentPower(String vmId) {
		Item i = client.getPowerFromVM(Dictionary.ITEM_POWER_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "W";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
						" " + units);
			}
			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_POWER_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_POWER_KEY + " value for VM " + vmId);
		}
	}

	
	private void publishCurrentEnergy(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_ENERGY_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "Wh";
			if (value>1000){
				value = value/1000;
				value = eu.ascetic.asceticarchitecture.iaas.zabbixApi.utils.Dictionary.round(value, 2);
				units = "KWh";
			}
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + value + " " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + value + " " + units);
			}
			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_ENERGY_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_ENERGY_KEY + " value for VM " + vmId);
		}
	}	
	
	private void publishCurrentIdlePower(String vmId) {
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_IDLE_POWER_KEY, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "W";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
						" " + units);
			}
			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_IDLE_POWER_NAME, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_IDLE_POWER_KEY + " value for VM " + vmId);
		}
	}
	
	private void publishCurrentNetPower(String vmId) {
		Item powerItem = client.getPowerFromVM(Dictionary.ITEM_POWER_KEY, vmId);
		Item idlePowerItem = client.getItemByKeyFromHost(Dictionary.ITEM_IDLE_POWER_KEY, vmId);
		if (powerItem != null && idlePowerItem != null && powerItem.getLastClock() != 0 && idlePowerItem.getLastClock() != 0){
			double powerValue =  Double.parseDouble(powerItem.getLastValue());
			double idlePowerValue = Double.parseDouble(idlePowerItem.getLastValue());
			double netPowerValue = powerValue - idlePowerValue;
			String units = "W";
			logger.info("VM " + vmId + " --> " + Dictionary.ITEM_NET_POWER_NAME +  ": " + netPowerValue + 
					" " + units);
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + vmId + " --> " + Dictionary.ITEM_NET_POWER_NAME +  ": " + netPowerValue +  
						" " + units);
			}
			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_NET_POWER_NAME, netPowerValue, units, powerItem.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_NET_POWER_NAME + " value for VM " + vmId);
		}
	}

	public String getHostname(){
		return hostname;
	}
	
}
