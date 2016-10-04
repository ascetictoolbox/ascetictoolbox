package eu.ascetic.utils.metricpusher.pusher;

import java.util.List;

import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import eu.ascetic.amqp.client.AmqpMessageProducer;
import eu.ascetic.amqp.client.AmqpMessageReceiver;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Item;
import eu.ascetic.utils.metricpusher.amqp.AmqpProducer;
import eu.ascetic.utils.metricpusher.conf.Dictionary;

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
 * This class is developed by testing the right working of the application
 *
 */

public class Tester {

	private static ZabbixClient client;
	private static Logger logger = Logger.getLogger(Tester.class);
	
	public static void main(String [] args) throws Exception{
		client = new ZabbixClient();
//		AmqpMessageReceiver receiver = new AmqpMessageReceiver("guest", "guest", "vm." + vm.getHostid() + ".item.memory");
//		publishCurrentMemory("0c1b61e8-c383-4a4f-9ef0-b3ee8eb5cbab");
		publishCurrentPower("0c1b61e8-c383-4a4f-9ef0-b3ee8eb5cbab");
		publishCurrentIdlePower("0c1b61e8-c383-4a4f-9ef0-b3ee8eb5cbab");
		publishCurrentNetPower("0c1b61e8-c383-4a4f-9ef0-b3ee8eb5cbab");
		
		
		Thread.sleep(1000l);
//		TextMessage message = receiver.getLastMessage();
		
//		System.out.println(message.getText());
		
		
//		receiver.close();
	}
	
	private static void publishCurrentMemory(String vmId) {
		String item = "memory";
		Item i = client.getItemByKeyFromHost(item, vmId);
		if (i != null && i.getLastClock() != 0){
			long value =  Long.parseLong(i.getLastValue());
			String units = "bytes";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
			System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Long.parseLong(i.getLastValue()) + 
					" " + units);
//			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, item, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + item + " value for VM " + vmId);
		}	
	}
	
	private static void publishCurrentIdlePower(String vmId) {
		String item = "idle.power";
		Item i = client.getItemByKeyFromHost(item, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "W";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
//			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, item, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + item + " value for VM " + vmId);
		}	
	}
	
	private static void publishCurrentPower(String vmId) {
		String item = "power";
		Item i = client.getPowerFromVM(item, vmId);
		if (i != null && i.getLastClock() != 0){
			double value =  Double.parseDouble(i.getLastValue());
			String units = "W";
			logger.info("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
			System.out.println("VM " + vmId + " --> " + i.getName() +  ": " + Double.parseDouble(i.getLastValue()) + 
					" " + units);
//			AmqpProducer.sendCurrentLongValueForItemInHostMessage(vmId, item, value, units, i.getLastClock());
		}
		else {
			logger.info("No " + item + " value for VM " + vmId);
		}	
	}
	
	
	private static void publishCurrentNetPower(String vmId) {
		Item powerItem = client.getPowerFromVM(Dictionary.ITEM_POWER_KEY, vmId);
		Item idlePowerItem = client.getItemByKeyFromHost(Dictionary.ITEM_IDLE_POWER_KEY, vmId);
		if (powerItem != null && idlePowerItem != null && powerItem.getLastClock() != 0 && idlePowerItem.getLastClock() != 0){
			double powerValue =  Double.parseDouble(powerItem.getLastValue());
			double idlePowerValue = Double.parseDouble(idlePowerItem.getLastValue());
			double netPowerValue = powerValue - idlePowerValue;
			String units = "W";
			logger.info("VM " + vmId + " --> " + Dictionary.ITEM_NET_POWER_NAME +  ": " + netPowerValue + 
					" " + units);
		
				System.out.println("VM " + vmId + " --> " + Dictionary.ITEM_NET_POWER_NAME +  ": " + netPowerValue +  
						" " + units);
			
//			AmqpProducer.sendCurrentDoubleValueForItemInHostMessage(vmId, Dictionary.ITEM_IDLE_POWER_NAME, netPowerValue, units, powerItem.getLastClock());
		}
		else {
			logger.info("No " + Dictionary.ITEM_NET_POWER_NAME + " value for VM " + vmId);
		}
	}
}
