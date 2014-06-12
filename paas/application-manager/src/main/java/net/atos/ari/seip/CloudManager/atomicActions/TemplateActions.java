package net.atos.ari.seip.CloudManager.atomicActions;

import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;

import org.apache.log4j.Logger;


public class TemplateActions {
	
	Logger log = Logger.getLogger(this.getClass().getName());
	CloudComputingManager ccManager;
		
	public TemplateActions(String secret, String baseurl) {
		super();
		ccManager = new CloudComputingManager(secret, baseurl);
	}

	public String createTemplate(String vmName, String imageName, int cpu, int vcpu, int mem, String net ) {
		return ccManager.createTemplate(vmName, imageName, cpu, vcpu, mem, net);
	}
	
	public String updateTemplate(int newTempID, String updated) {
		return ccManager.updateTemplate(newTempID, updated);
	}
	
}
