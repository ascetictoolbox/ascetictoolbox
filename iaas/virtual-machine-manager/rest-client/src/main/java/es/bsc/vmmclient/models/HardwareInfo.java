/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.bsc.vmmclient.models;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * Copyright (C) 2013-2014  Barcelona Supercomputing Center 
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
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 */
public class HardwareInfo {
    private String hostname;
    private String cpuVendor;
    private String cpuArchitecture;
    private String cpuModel;
    private String diskType;
    
    public HardwareInfo() { }
    
    public HardwareInfo(String hostname, String cpuVendor, String cpuArchitecture, String cpuModel, String diskType) {
        this.hostname = hostname;
        this.cpuVendor = cpuVendor;
        this.cpuArchitecture = cpuArchitecture;
        this.cpuModel = cpuModel;
        this.diskType = diskType;
    }
    
    /**
     * 
     * @param hwinfo1
     * @param hwinfo2
     * @return 
     */
    public static Map<String, HardwareInfo> merge(
        Map<String, HardwareInfo> hwinfo1, Map<String, HardwareInfo> hwinfo2) {
        
        for(Entry<String,HardwareInfo> h : hwinfo2.entrySet()){
            HardwareInfo h1 = hwinfo1.get(h.getKey());
            HardwareInfo h2 = h.getValue();
            
            if(h1 != null && h2.getCpuArchitecture() != null){
                h1.setCpuArchitecture( h2.getCpuArchitecture() );
            }
            
            if(h1 != null && h2.getCpuVendor() != null){
                h1.setCpuVendor( h2.getCpuVendor() );
            }
            
            if(h1 != null && h2.getCpuModel() != null){
                h1.setCpuModel( h2.getCpuModel() );
            }
            
            if(h1 != null && h2.getDiskType() != null){
                h1.setDiskType( h2.getDiskType() );
            }
            
            hwinfo1.put(h.getKey(), h1);
        }
        
        return hwinfo1;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the cpuVendor
     */
    public String getCpuVendor() {
        return cpuVendor;
    }

    /**
     * @param cpuVendor the cpuVendor to set
     */
    public void setCpuVendor(String cpuVendor) {
        this.cpuVendor = cpuVendor;
    }

    /**
     * @return the cpuArchitecture
     */
    public String getCpuArchitecture() {
        return cpuArchitecture;
    }

    /**
     * @param cpuArchitecture the cpuArchitecture to set
     */
    public void setCpuArchitecture(String cpuArchitecture) {
        this.cpuArchitecture = cpuArchitecture;
    }

    /**
     * @return the cpuModel
     */
    public String getCpuModel() {
        return cpuModel;
    }

    /**
     * @param cpuModel the cpuModel to set
     */
    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
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
}
