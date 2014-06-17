package net.atos.ari.seip.CloudManager.manager;

import org.apache.log4j.Logger;
//import org.opennebula.client.Client;
//import org.opennebula.client.OneResponse;
//import org.opennebula.client.template.Template;
//import org.opennebula.client.vm.VirtualMachine;

public class TemplateManager {

	Logger log = Logger.getLogger(this.getClass().getName());
//	Client client;
	
	public TemplateManager(/*Client c*/){
//		client = c;
	}
	
	public String create(String vmName, String imageName, int cpu, int vcpu, int mem, String net ) {
//	    String Template = " NAME   = " +
//	    		vmName +
//	    		"\n" +
//	    		"CPU    = " +
//	    		cpu +
//	    		"\n" +
//	    		"VCPU   = " +
//	    		vcpu +
//	    		"\n" +
//	    		"MEMORY = " +
//	    		mem +
//	    		"\n" +
//	    		"OS = [ \n" +
//	    		"\tARCH = \"x86_64\",\n" +
//	    		"\tMACHINE = \"rhel6.3.0\",\n" +
//	    		"\tBOOT = \"hd\"  ]\n" +
//	    		"DISK = [ \n" +
//	    		"\tIMAGE = \"" +
//	    		imageName +
//	    		"\",\n" +
//	    		"\tIMAGE_UNAME = \"oneadmin\",\n" +
//	    		"BUS = \"virtio\",\n" +
//	    		"DRIVER = \"qcow2\" ]\n" +
//	    		"NIC = [ \n" +
//	    		"\tNETWORK = \"" +
//	    		net +
//	    		"\",\n" +
//	    		"NETWORK_UNAME = \"oneadmin\",\n" +
//	    		"MODEL = \"virtio\" ]\n" +
//	    		"RAW = [\n" +
//	    		"\ttype = \"kvm\",\n" +
//	    		"\tdata = \"<devices>\n" +
//	    		"\t<serial type='pty'>\n" +
//	    		"\t<source path='/dev/pts/1'/>\n" +
//	    		"\t<target port='0'/>\n" +
//	    		"\t<alias name='serial0'/>\n" +
//	    		"\t</serial>\n" +
//	    		"\t<console type='pty' tty='/dev/pts/1'>\n" +
//	    		"\t<source path='/dev/pts/1'/>\n" +
//	    		"\t<target type='serial' port='0'/>\n" +
//	    		"\t<alias name='serial0'/>\n" +
//	    		"\t</console>\n" +
//	    		"\t</devices>\"\n" +
//	    		"]";
	    return null; //Template;
	}
	
	
	public String update(int newTempID, String updated) {
//		try {
//			
//			log.debug("Trying to restart the virtual machine... ");
//			Template template = new Template(newTempID, client);
//			 OneResponse rc = template.update(updated);
//		    if( rc.isError() )
//            {
//              log.error( "Instance restart: " + rc.getErrorMessage() );
//            }
//        	else
//            {
//        		log.debug( "Instance restart: " + rc.getMessage());
//            }
//		    
//		    //vm.lcmStateStr();
//		    return template.getId();
//		    
//		} catch (Exception e) {
//			log.error("error update Template " + newTempID);
//			return null;
//		}
		return null;
	}
	
	
	public String getInfo(int templateId) {	    
//		try {
//			
//			log.debug("Trying to getInfo of the template... ");
//			Template template = new Template(templateId, client);
//		    OneResponse rc = template.info();
//		    return rc.getMessage();
//		    
//		    
//		} catch (Exception e) {
//			log.error("error getInfo Template " + templateId);
//			return null;
//		}
		
		return null;
	}
	
}
