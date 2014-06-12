package net.atos.ari.seip.CloudManager.atomicActions;

import net.atos.ari.seip.CloudManager.manager.CloudComputingManager;

import org.apache.log4j.Logger;

public class InstanceActions {
	
	Logger log = Logger.getLogger(this.getClass().getName());
	CloudComputingManager ccManager;

	
	public InstanceActions(String secret, String baseurl){
		super();
		ccManager = new CloudComputingManager(secret, baseurl);
	}
	

	public String createInstance(String template) {	
		return ccManager.createInstance(template);		
	}
	
	public String restartInstance(int newVMID) {
		return ccManager.restartInstance(newVMID);
	}
	
	public int deleteInstance(int newVMID) {
		return ccManager.deleteInstance(newVMID);
	}
	
	public int shutdownInstance(int newVMID) {
		return ccManager.shutdownInstance(newVMID);
	}
	
	public int stopInstance(int newVMID) {
		return ccManager.stopInstance(newVMID);
	}
	
	public int migrateInstance(int newVMID, int host) {
		return ccManager.migrateInstance(newVMID, host);
	}
	
	public int livemigrateInstance(int newVMID, int host) {
		return ccManager.livemigrateInstance(newVMID, host);
	}
	
	public String getdiskInstance(int newVMID) {
		return ccManager.getdiskInstance(newVMID);
	}
	
	public String getinfoInstance(int newVMID) {
		return ccManager.getinfoInstance(newVMID);
	}
	
}
