package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;


/**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author: David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * @email david.rojoa@atos.net 
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class Vm {

	private String ovfId;
    private String name;
    private String image; // It can be an ID or a URL
    private int cpus;
    private int ramMb;
    private int diskGb;
    private String initScript;
    private String applicationId;
    
    public Vm() {}

    /**
     * Class constructor.
     * @param name The name of the instance.
     * @param image The ID of the image or a URL containing it.
     * @param cpus The number of CPUs.
     * @param ramMb The amount of RAM in MB.
     * @param diskGb The size of the disk in GB.
     * @param initScript Script that will be executed when the VM is deployed.
     */
    public Vm(String name, String image, int cpus, int ramMb, int diskGb, String initScript, String applicationId) {
        this.name = name;
        this.image = image;
        setCpus(cpus);
        setRamMb(ramMb);
        setDiskGb(diskGb);
        setInitScript(initScript);
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setInstanceName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        if (cpus <= 0) {
            throw new IllegalArgumentException("The number of cpus has to be greater than 0");
        }
        this.cpus = cpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public void setRamMb(int ramMb) {
        if (ramMb <= 0) {
            throw new IllegalArgumentException("The amount of memory has to be greater than 0");
        }
        this.ramMb = ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public void setDiskGb(int diskGb) {
        if (diskGb <= 0) {
            throw new IllegalArgumentException("The amount of disk size has to be greater than 0");
        }
        this.diskGb = diskGb;
    }

    public String getInitScript() {
        return initScript;
    }

    public void setInitScript(String initScript) {
        // If a path for an init script was specified
        if (initScript != null) {
            // Check that the path is valid and the file can be read
//            File f = new File(initScript);
//            if (!f.isFile() || !f.canRead()) {
//                throw new IllegalArgumentException("The path for the init script is not valid");
//            }
        }
        this.initScript = initScript;
    }

    public String getApplicationId() {
        return applicationId;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

	public String getOvfId() {
		return ovfId;
	}

	public void setOvfId(String ovfId) {
		this.ovfId = ovfId;
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
        } else if(!this.ovfId.equals(vm.ovfId)) {
        	return false;
        }
        
        if(this.name == null) {
        	if(vm.getName() != null ) {
        		return false;
        	}
        } else if(!this.name.equals(vm.name)) {
        	return false;
        }
        
        if(this.image == null) {
        	if(vm.getImage() != null) {
        		return false;
        	}
        } else if(!this.image.equals(vm.image)) {
        	return false;
        }
        
        if(this.initScript == null) {
        	if(vm.getInitScript() != null) {
        		return false;
        	}
        } else if(!this.initScript.equals(vm.initScript)) {
        	return false;
        }
        
        if(this.applicationId == null) {
        	if(vm.getApplicationId() != null) {
        		return false;
        	}
        } else if(!this.applicationId.equals(vm.applicationId)) {
        	return false;
        }
        
        if(this.cpus == vm.getCpus()
           && this.ramMb == vm.getRamMb()
           && this.diskGb == vm.getDiskGb()) {
        	return true;
        } else {
        	return false;
        }
	}
}
