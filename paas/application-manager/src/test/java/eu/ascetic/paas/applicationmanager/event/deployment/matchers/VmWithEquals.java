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
			            String slaId,
			            boolean floatingIp) {
		super(name, image, cpus, ramMb, diskGb, swapMb, initScript, applicationId, ovfId, slaId, floatingIp);
	}

	@Override
	public boolean equals(Object obj) {
		
        if (!(obj instanceof Vm))
            return false;
        if (obj == this)
            return true;

        Vm vm = (Vm) obj;
        
        if(this.getOvfId() == null) {
        	if(vm.getOvfId() != null) {
        		return false;
        	}
        } else if(!this.getOvfId().equals(vm.getOvfId())) {
        	return false;
        }
        
        
        if(this.getName() == null) {
        	if(vm.getName() != null ) {
        		return false;
        	}
        } else if(!this.getName().equals(vm.getName())) {
        	return false;
        }
        
        
        if(this.getImage() == null) {
        	if(vm.getImage() != null) {
        		return false;
        	}
        } else if(!this.getImage().equals(vm.getImage())) {
        	return false;
        }
        
        if(this.getInitScript() == null) {
        	if(vm.getInitScript() != null) {
        		return false;
        	}
        } else if(!this.getInitScript().equals(vm.getInitScript())) {
        	return false;
        }
        
        if(this.getApplicationId() == null) {
        	if(vm.getApplicationId() != null) {
        		return false;
        	}
        } else if(!this.getApplicationId().equals(vm.getApplicationId())) {
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
        
        if(this.getCpus() == vm.getCpus()
           && this.getRamMb() == vm.getRamMb()
           && this.getDiskGb() == vm.getDiskGb()
           && this.getSwapMb() == vm.getSwapMb()
           && this.needsFloatingIp() == vm.needsFloatingIp()) {
        	return true;
        } else {
        	return false;
        }
	}
}
