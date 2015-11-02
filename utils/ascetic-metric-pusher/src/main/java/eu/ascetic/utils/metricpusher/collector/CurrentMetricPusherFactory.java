package eu.ascetic.utils.metricpusher.collector;

import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.utils.metricpusher.amqp.AmqpProducer;
import eu.ascetic.utils.metricpusher.conf.Configuration;
import eu.ascetic.utils.metricpusher.conf.Dictionary;
import eu.ascetic.utils.metricpusher.pusher.MetricPusher;

/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
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
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class is the responsible of publishing monitoring data from virtual machines into IaaS layer
 *
 */

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
					publishCurrentCpu(vm.getHost());
					publishCurrentMemory(vm.getHost());
					publishCurrentTransmittedBytes(vm.getHost());
					publishCurrentReceivedBytes(vm.getHost());
					if (MetricPusher.SHOW_DEBUG_TRACES){
						System.out.println("********************************************************");
					}
				}
				if (MetricPusher.SHOW_DEBUG_TRACES){
					System.out.println("----------------------------------------------------------");
					System.out.println("------------------- END OF VMS ---------------------------");
					System.out.println("----------------------------------------------------------");
				}
				//wait 60 seconds to push more data to Communication middleware
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
		Item i = client.getItemByKeyFromHost(Dictionary.ITEM_POWER_KEY, vmId);
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

}
