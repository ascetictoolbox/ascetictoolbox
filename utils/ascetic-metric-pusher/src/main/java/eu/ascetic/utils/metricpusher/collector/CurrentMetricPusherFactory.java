package eu.ascetic.utils.metricpusher.collector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.client.ZabbixClient;
import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import eu.ascetic.utils.metricpusher.conf.Configuration;
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
	private Set<String> vmsSet = new HashSet<String>();
	private List<VmAgentMonitor> vmAgentMonitorList = new ArrayList<VmAgentMonitor>();
	
	public CurrentMetricPusherFactory(){
		client = new ZabbixClient();
	}
	
	@SuppressWarnings("unchecked")  
	public void run()  
	{  
		if (MetricPusher.SHOW_DEBUG_TRACES){
			System.out.println("Launching CurrentMetricPusherFactory...");
		}
		logger.info("Launching CurrentMetricPusherFactory...");
		List<Host> vms = null; 
		Set<String> existingVms = new HashSet<String>();
		while (true){
			try {		
				vms = client.getVms();
				if (MetricPusher.SHOW_DEBUG_TRACES){
					if (vms == null){
						System.out.println("VMs NOT collected from Zabbix" );
					}
					else {
						System.out.println("VMs successfully collected from Zabbix = " + vms.size()+ " VMs");
					}
					
				}
				for (Host vm : vms){
					if (!vmsSet.contains(vm.getHost())){
						if (MetricPusher.SHOW_DEBUG_TRACES){
							System.out.println("New VM found: " + vm.getHost());
						}
						//new VM
						vmsSet.add(vm.getHost());
						VmAgentMonitor vmAgentMonitor = new VmAgentMonitor(vm.getHost());
						vmAgentMonitor.start();
						vmAgentMonitorList.add(vmAgentMonitor);
						if (MetricPusher.SHOW_DEBUG_TRACES){
							System.out.println("VM " + vm.getHost() + " successfully added");
						}
					}	
					else {
						if (MetricPusher.SHOW_DEBUG_TRACES){
							System.out.println(vm.getHost() + " VM exists in the system");
						}
					}
					existingVms.add(vm.getHost());
				}
				clearDeletedVms(existingVms);
				
				//clear existing VMs set for next iteration
				existingVms.clear();
				//empty vms list
				vms = null;
				//wait 40 seconds to push more data to Communication middleware
				Thread.sleep(Long.parseLong(Configuration.publishFrequency));
			}
			catch (Exception e){
				logger.error(e.getMessage());
			}
		}
	}  
	

	private void clearDeletedVms(Set<String> existingVms){
		Set<String> vmsToDelete = new HashSet<String>();
		vmsToDelete.addAll(vmsSet);
		vmsToDelete.removeAll(existingVms);
		if (!vmsToDelete.isEmpty()){
			for (String vm : vmsToDelete){
				removeVm(vm);
			}
			
		}
	}

	private void removeVm(String hostname){
		boolean deleted = false;
		int i = 0;
		while (vmAgentMonitorList.size() > i && !deleted){
			if (vmAgentMonitorList.get(i).getHostname().equals(hostname)){
				vmAgentMonitorList.get(i).stop();
				vmAgentMonitorList.remove(i);
				deleted = true;
				
			}
			i++;
		}
		if (deleted){
			vmsSet.remove(hostname);
			logger.info("VM " + hostname + " deleted succesfully");
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + hostname + " successfully deleted");
			}
		}
		else {
			logger.warn("VM " + hostname + " not deleted succesfully");
			if (MetricPusher.SHOW_DEBUG_TRACES){
				System.out.println("VM " + hostname + " NOT successfully deleted");
			}
		}
	}
	
}
