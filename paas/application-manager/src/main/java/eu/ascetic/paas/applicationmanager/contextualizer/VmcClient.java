package eu.ascetic.paas.applicationmanager.contextualizer;

import org.apache.log4j.Logger;

import eu.ascetic.paas.applicationmanager.conf.Configuration;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.VmcApi;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;

/**
 * Class that connects to the VM Contextualizer API
 * @author David Rojo - Atos
 *
 */
public class VmcClient {
	
	private VmcApi vmcApi;
	private static Logger logger = Logger.getLogger(VmcClient.class);
	
	public VmcClient(OvfDefinition ovfDefinition){
		GlobalConfiguration globalConfiguration = null;
		try {
			globalConfiguration = new GlobalConfiguration(Configuration.vmcontextualizerConfigurationFileDirectory);
		} catch (Exception e) {
			logger.info("Error creating globalConfiguration object to connect with VM contextualizer. Details: " + e.getMessage());
		}
		if (globalConfiguration != null){
			vmcApi = new VmcApi(globalConfiguration);
		}
	}
	
	public VmcApi getVmcClient(){
		return vmcApi;
	}
}
