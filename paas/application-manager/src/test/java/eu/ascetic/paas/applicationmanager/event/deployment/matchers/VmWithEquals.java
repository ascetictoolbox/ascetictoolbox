package eu.ascetic.paas.applicationmanager.event.deployment.matchers;

import es.bsc.vmmclient.models.Vm;



public class VmWithEquals extends Vm {
	
	public VmWithEquals(String name,
			            String image,
			            int cpus,
			            int ramMb,
			            int diskGb,
			            int swapMb,
			            String initScript,
			            String applicationId,
			            String ovfId,
			            String slaId) {
		super(name, image, cpus, ramMb, diskGb, swapMb, initScript, applicationId, ovfId, slaId);
	}

	@Override
	public boolean equals(Object obj) {
		
        if (!(obj instanceof Vm))
            return false;
        if (obj == this)
            return true;

        Vm vm = (Vm) obj;
        
        if(this.ovfId == null) {
        	if(vm.getOvfId() != null) {
        		return false;
        	}
        } else if(!this.ovfId.equals(vm.getOvfId())) {
        	return false;
        }
        
        
        if(this.name == null) {
        	if(vm.getName() != null ) {
        		return false;
        	}
        } else if(!this.name.equals(vm.getName())) {
        	return false;
        }
        
        
        if(this.image == null) {
        	if(vm.getImage() != null) {
        		return false;
        	}
        } else if(!this.image.equals(vm.getImage())) {
        	return false;
        }
        
        if(this.initScript == null) {
        	if(vm.getInitScript() != null) {
        		return false;
        	}
        } else if(!this.initScript.equals(vm.getInitScript())) {
        	return false;
        }
        
        if(this.applicationId == null) {
        	if(vm.getApplicationId() != null) {
        		return false;
        	}
        } else if(!this.applicationId.equals(vm.getApplicationId())) {
        	return false;
        } 
        
//        System.out.println("SLA ID: " + this.slaId + " VM SLAID: " + vm.getSlaId());
//        
//        if(this.slaId == null) {
//        	if(vm.getSlaId() != null) {
//        		return false;
//        	}
//        } else if(!this.slaId.equals(vm.getSlaId())) {
//        	return false;
//        }
        
        if(this.cpus == vm.getCpus()
           && this.ramMb == vm.getRamMb()
           && this.diskGb == vm.getDiskGb()
           && this.swapMb == vm.getSwapMb()) {
        	return true;
        } else {
        	return false;
        }
	}
}
