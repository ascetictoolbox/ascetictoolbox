package net.atos.ari.seip.CloudManager.manager;

import org.apache.log4j.Logger;
import org.opennebula.client.Client;
import org.opennebula.client.ClientConfigurationException;
import org.opennebula.client.OneResponse;
import org.opennebula.client.user.User;
import org.opennebula.client.vm.VirtualMachinePool;

public class CloudComputingManager {

	Logger log = Logger.getLogger(this.getClass().getName());	
	Client client = null;
	String secret;
	String baseurl;
	InstanceManager instanceManager = null;
	TemplateManager templateManager = null;
	
	public CloudComputingManager(String secret, String baseurl){
		this.secret = secret;
		this.baseurl = baseurl;
	}
	
	private Client getClient(){
		if (client == null) {
			try {
				client = new Client(secret, baseurl);
			} catch (ClientConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		return client;
	}
	
	
	private InstanceManager getInstanceManager(){
		if (instanceManager == null){
			instanceManager = new InstanceManager(getClient());
		}
		return instanceManager;
	}
	
	private TemplateManager getTemplateManager(){
		if (templateManager == null){
			templateManager = new TemplateManager(getClient());
		}
		return templateManager;
	}
	
	
	public String createInstance(String template) {
		return getInstanceManager().create(template);
	}
		
	public String restartInstance(int newVMID) {
		return getInstanceManager().restart(newVMID);
	}
	
	public int deleteInstance(int newVMID) {
		return getInstanceManager().delete(newVMID);
	}
	
	public int shutdownInstance(int newVMID) {
		return getInstanceManager().shutdown(newVMID);
	}
	
	public int stopInstance(int newVMID) {
		return getInstanceManager().stop(newVMID);
	}
	
	public int migrateInstance(int newVMID, int host) {
		return getInstanceManager().migrate(newVMID, host);
	}
	
	public int livemigrateInstance(int newVMID, int host) {
		return getInstanceManager().liveMigrate(newVMID, host);
	}
	
	public String getdiskInstance(int newVMID) {
		return getInstanceManager().getDisk(newVMID);
	}
	
	public String getinfoInstance(int newVMID) {
		return getInstanceManager().getInfo(newVMID);
	}
	
	public String getStatusInstance(int newVMID) {
		return getInstanceManager().getStatus(newVMID);
	} 
	
	public String createTemplate(String vmName, String imageName, int cpu, int vcpu, int mem, String net ) {
		return getTemplateManager().create(vmName, imageName, cpu, vcpu, mem, net);
	}
	
	public String updateTemplate(int newTempID, String updated) {
		return getTemplateManager().update(newTempID, updated);
	}
	
	public String getInfoTemplate(int templateId){
		return getTemplateManager().getInfo(templateId);
	}
	
	public void testUserConnection() {
		User user = new User (-1, getClient());
    	OneResponse res = user.info();
    	if( res.isError() )
        {
            System.out.println( "User name:" + res.getErrorMessage());
        }
        else
        {
        	System.out.println("Connected!");
            System.out.println( "User name: " + user.getName() );
        }    	
	}
	
	
	public void testVirtualMachinePool() {
		VirtualMachinePool vpool = new VirtualMachinePool(getClient());
	    OneResponse rpool = vpool.infoAll(getClient());    	
	    if( rpool.isError() )
	    {
	    	System.out.println( "Vpool: " + rpool.getErrorMessage() );
	    }
	    else
	    {
	    	System.out.println( "Vpool: " + rpool.getMessage() );

	    }
	}
	
}
