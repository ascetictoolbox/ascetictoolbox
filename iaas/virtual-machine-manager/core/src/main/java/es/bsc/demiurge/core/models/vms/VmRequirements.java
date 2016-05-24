/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.models.vms;

import com.google.common.base.MoreObjects;
import java.util.HashMap;
import java.util.Map;

public class VmRequirements {

    protected final int cpus;
    protected final int ramMb;
    protected final int diskGb;
    protected final int swapMb;
    private boolean autoConfirmResize;
    
    private String processorArchitecture = null;
    private String processorBrand = null;
    private String processorModel = null;
    private String diskType = null;

    public VmRequirements(int cpus, int ramMb, int diskGb, int swapMb) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.autoConfirmResize = true;
    }
    
    public VmRequirements(int cpus, int ramMb, int diskGb, int swapMb, String processorArchitecture, String processorBrand, String diskType) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.processorArchitecture = processorArchitecture;
        this.processorBrand = processorBrand;
        this.diskType = diskType;
        this.autoConfirmResize = true;
    }
    
    public VmRequirements(int cpus, int ramMb, int diskGb, int swapMb, String processorArchitecture, String processorBrand, String processorModel, String diskType) {
        this.cpus = cpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.swapMb = swapMb;
        this.processorArchitecture = processorArchitecture;
        this.processorBrand = processorBrand;
        this.processorModel = processorModel;
        this.diskType = diskType;
        this.autoConfirmResize = true;
    }

    public int getCpus() {
        return cpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public int getSwapMb() {
        return swapMb;
    }

    /**
     * @return checks if confirmResize has to be done automatically
     */
    public boolean isAutoConfirm() {
        return autoConfirmResize;
    }

    /**
     * @param autoConfirm if set to true confirmResize will be done automatically
     */
    public void setAutoConfirm(boolean autoConfirm) {
        this.autoConfirmResize = autoConfirm;
    }

    /**
     * @return the processorArchitecture
     */
    public String getProcessorArchitecture() {
        return processorArchitecture;
    }

    /**
     * @param processorArchitecture the processorArchitecture to set
     */
    public void setProcessorArchitecture(String processorArchitecture) {
        this.processorArchitecture = processorArchitecture;
    }

    /**
     * @return the processorBrand
     */
    public String getProcessorBrand() {
        return processorBrand;
    }

    /**
     * @param processorBrand the processorBrand to set
     */
    public void setProcessorBrand(String processorBrand) {
        this.processorBrand = processorBrand;
    }

    /**
     * @return the processorModel
     */
    public String getProcessorModel() {
        return processorModel;
    }

    /**
     * @param processorModel the processorModel to set
     */
    public void setProcessorModel(String processorModel) {
        this.processorModel = processorModel;
    }

    /**
     * @return the diskType
     */
    public String getDiskType() {
        return diskType;
    }

    /**
     * @param diskType the diskType to set
     */
    public void setDiskType(String diskType) {
        this.diskType = diskType;
    }
    
    /**
     * Delivers those requirements that are optional.
     * 
     * @return the list of optional requirements
     */
    public Map<String, String> getOptionalRequirements() {
        Map<String, String> requirements = new HashMap<>();
        if(this.processorArchitecture != null){
            requirements.put("processor_architecture", processorArchitecture);
        }
        if(this.processorBrand != null){
            requirements.put("processor_brand", processorBrand);
        }
        if(this.processorModel != null){
            requirements.put("processor_model", processorModel);
        }
        if(diskType != null){
            requirements.put("disk_type", diskType);
        }
        
        return requirements;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cpus", cpus)
                .add("ramMb", ramMb)
                .add("diskGb", diskGb)
                .add("swapMb", swapMb)
                .add("processorArchitecture", processorArchitecture)
                .add("processorBrand", processorBrand)
                .add("processorModel", processorModel)
                .add("diskType", diskType)
                .toString();
    }
}