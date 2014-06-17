package net.atos.ari.seip.CloudManager.manager;

import org.apache.log4j.Logger;
//import org.opennebula.client.Client;
//import org.opennebula.client.OneResponse;
//import org.opennebula.client.vm.VirtualMachine;

public class InstanceManager {

	Logger log = Logger.getLogger(this.getClass().getName());
//	Client client;
	
	public InstanceManager(/*Client c*/){
		//client = c;
	}
	
	public String create(String template) {
		try {			
			log.debug("Trying to allocate the virtual machine... ");
//			OneResponse rc = VirtualMachine. allocate(client, template);
//			if( rc.isError() )
//			{
//				log.error("Trying to allocate VM: FAILED ");
//				throw new Exception( rc.getErrorMessage() );
//			}

			// The response message is the new VM's ID
			String newVMID = "";//rc.getMessage();
			log.debug("OK instanceID " + newVMID + ".");

			return newVMID;

		} catch (Exception e) {
			log.error("error creating Instance " + template);
			return null;
		}
	}
	
	
	public String restart(int newVMID) {
		try {
			
			log.debug("Trying to restart the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    OneResponse rc = vm.restart();
//		    if( rc.isError() )
//            {
//              log.error( "Instance restart: " + rc.getErrorMessage() );
//            }
//        	else
//            {         
//        		 log.debug( "Instance restart: " + rc.getMessage());
//            }
		    
		    return null; //vm.getId();
		    
		} catch (Exception e) {
			log.error("error restart Instance " + newVMID);
			return null;
		}
	}
	
	
	public int delete(int newVMID) {
		try {			
			log.debug("Trying to delete the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    vm.finalizeVM();

		    return newVMID;
		    
		} catch (Exception e) {
			log.error("error delete Instance " + newVMID);
			return newVMID;
		}
	}
	
	
	public int shutdown(int newVMID) {
		
		try {
			
			log.debug("Trying to shutdown the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    vm.shutdown();
//		    
//		    //vm.lcmStateStr();
		    
		    return newVMID;
		    
		} catch (Exception e) {
			log.error("error shutdown Instance " + newVMID);
			return newVMID;
		}
	}
	
	public int stop(int newVMID) {
		try {
			
			log.debug("Trying to stop the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    vm.stop();
//		    
//		    //vm.lcmStateStr();
		    
		    return newVMID;
		    
		} catch (Exception e) {
			log.error("error stop Instance " + newVMID);
			return newVMID;
		}
	}
	
	public int migrate(int newVMID, int host) {
		try {
			
			log.debug("Trying to migrate the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    vm.migrate(host);
//		    
//		    //vm.lcmStateStr();
		    
		    return newVMID;
		    
		} catch (Exception e) {
			log.error("error migrate Instance " + newVMID);
			return newVMID;
		}
	}
	
	public int liveMigrate(int newVMID, int host) {
		try {
			
			log.debug("Trying to migrate the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    vm.liveMigrate(host);
//		    
//		    //vm.lcmStateStr();
		    
		    return newVMID;
		    
		} catch (Exception e) {
			log.error("error migrate Instance " + newVMID);
			return newVMID;
		}
	}
	
	
	public String getDisk(int newVMID) {
//		try {
//			
//			log.debug("Trying to getDisk of the virtual machine... ");
////			VirtualMachine vm = new VirtualMachine(newVMID, client);
////		    //vm.info();
////		    return vm.xpath("//template/disk/source");
////		    
////		    //vm.lcmStateStr();
//		    
//		} catch (Exception e) {
//			log.error("error getDisk Instance " + newVMID);
//			return null;
//		}
		
		return null;
	}
	
	
	public String getInfo(int newVMID) {
//		try {
//			
//			log.debug("Trying to getInfo of the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    //vm.info();
//		    OneResponse rc = vm.info();
//		    return rc.getMessage();
//		    
//		    
//		} catch (Exception e) {
//			log.error("error getInfo Instance " + newVMID);
//			return null;
//		}
		return null;
	}
	
	
	public String getStatus (int newVMID) {
//		try {
//			
//			log.debug("Trying to getInfo of the virtual machine... ");
//			VirtualMachine vm = new VirtualMachine(newVMID, client);
//		    return vm.status();		    
//		    
//		} catch (Exception e) {
//			log.error("error getStatus Instance " + newVMID);
//			return null;
//		}
		
		return null;
	}
}
