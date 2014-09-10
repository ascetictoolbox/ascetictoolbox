package eu.ascetic.paas.applicationmanager.vmmanager;

import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.model.Deployment;
import eu.ascetic.paas.applicationmanager.model.VM;
import eu.ascetic.paas.applicationmanager.vmmanager.client.VmManagerClientHC;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.Vm;
import eu.ascetic.paas.applicationmanager.vmmanager.datamodel.VmDeployed;

public class VmManagerUtils {

	private static Logger logger = Logger.getLogger(VmManagerUtils.class);
	
	
	
	/**
	 * Update vms id.
	 *
	 * @param deployment the deployment
	 * @param listNewVmIds the list new vm ids
	 * @return true, if successful
	 */
	public static boolean updateVms(VmManagerClientHC vmClient, Deployment deployment, List<String> listNewVmIds, List<Vm> vmsToDeploy){
		boolean updated = true;
		int index = 0;
		VmDeployed vmDeployed = null;
		try {
			while (index < listNewVmIds.size()){ //TODO Change this to a bucle for... with generics...
				VM vm = new VM();
				//update the vm id
				vm.setProviderVmId(listNewVmIds.get(index));
				//get data from vmDeployed in VM manager
				vmDeployed = vmClient.getVM(listNewVmIds.get(index));
				vm.setIp(vmDeployed.getIpAddress());
				vm.setStatus(vmDeployed.getState());
				String ovfId = getOvfID(vmDeployed.getName(), vmsToDeploy);
				vm.setOvfId(ovfId);
				logger.debug("Adding the following VM to the deployment: OVF-ID: " + vm.getOvfId() + " IP: " + vm.getIp() );
				//add the current VM to the deployment
				deployment.addVM(vm);					
				index++;
			}
		}
		catch (Exception e){
			logger.error("Error adding new VMs to a deployment. Error: " + e.getMessage());
			updated = false;
		}
		return updated;
	}
	
	protected static String getOvfID(String name, List<Vm> vmsToDeploy) {
		
		for(Vm vm : vmsToDeploy) {
			if(vm.getName().equals(name)) {
				return vm.getOvfId();
			}
		}
		
		return null;
	}
}
