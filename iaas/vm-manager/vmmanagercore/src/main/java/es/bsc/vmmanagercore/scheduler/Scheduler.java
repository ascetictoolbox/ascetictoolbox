package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;
import java.util.HashMap;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;


/**
 * Interface for the scheduling algorithms used by the VM Manager.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface Scheduler {

	/**
	 * Decides on which host deploy each of the VMs that need to be deployed
	 * @param vmDescriptions the description of the VMs that need to be scheduled
	 * @param hostsInfo information of the hosts of the infrastructure where the 
	 * VMs need to be deployed
	 * @return HashMap that contains for each VM description, the name of the host where
	 * the VM should be deployed according to the scheduling algorithm
	 */
	public HashMap<Vm, String> schedule(ArrayList<Vm> vmDescriptions, 
			ArrayList<HostInfo> hostsInfo);
	
}
