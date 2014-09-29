/**
 * Copyright 2014 Hewlett-Packard Development Company, L.P.                                         
 */

package datastructure;

import java.util.LinkedHashMap;
import org.slasoi.slamodel.primitives.CONST;

/**
 * Each request represents a full configuration of virtual machine (VM) request.
 * 
 * @author Kuan Lu
 */
public class Request extends VMAccessPoint {
    /**
     * VM name
     */
    private String vmName;
    /**
     * CPU and memory
     */
    private LinkedHashMap<String, Resource> resource;
    /**
     * Start time
     */
    private String startTime = "";
    /**
     * End time
     */
    private String freeAt = "";
    /**
     * Isolation
     */
    private boolean isolation;
    /**
     * Client type
     */
    private String clientType;
    /**
     * Persistence
     */
    private boolean persistence;
    /**
     * Image
     */
    private String image = "";
    /**
     * Location
     */
    private String location = "";
    /**
     * CPU Number
     */
    private CONST cpuNr;
    /**
     * Memory Number
     */
    private CONST memoryNr;
    /**
     * VM number
     */
    private CONST vmNumber;
    /**
     * VM hard disk size
     */
    private CONST harddiskNr;
    /**
     * VM bandwidth
     */
    private CONST bandwidth;

    public Request(String clientType) {
        this.setResource(new LinkedHashMap<String, Resource>());
        // TODO: the client type should be defined somewhere.
        this.setClientType(clientType);
    }

    /**
     * Gets location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * To evaluate whether the persistence is selected or not.
     */
    public boolean isPersistence() {
        return persistence;
    }

    /**
     * Sets persistence .
     */
    public void setPersistence(boolean persistence) {
        this.persistence = persistence;
    }

    /**
     * Gets image.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets image.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * To evaluate whether the isolation is selected or not.
     */
    public boolean isIsolation() {
        return isolation;
    }

    /**
     * Sets the isolation.
     */
    public void setIsolation(boolean isolation) {
        this.isolation = isolation;
    }

    /**
     * Gets client type.
     */
    public String getClientType() {
        return clientType;
    }

    /**
     * Gets the name of virtual machine.
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * Sets the name of virtual machine.
     */
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    /**
     * Gets the number of virtual machine.
     */
    public CONST getVmNumber() {
        return vmNumber;
    }

    /**
     * Sets the number of virtual machine.
     */
    public void setVmNumber(CONST vmNumber) {
        this.vmNumber = vmNumber;
    }

    /**
     * Gets the starting time.
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the starting time.
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the free time.
     */
    public String getFreeAt() {
        return freeAt;
    }

    /**
     * Sets the free time.
     */
    public void setFreeAt(String freeAt) {
        this.freeAt = freeAt;
    }

    /**
     * Gets the resource.
     */
    public LinkedHashMap<String, Resource> getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     */
    public void setResource(LinkedHashMap<String, Resource> resource) {
        this.resource = resource;
    }

    /**
     * Sets the client type.
     */
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    /**
     * Gets the number of CPU for specific virtual machine.
     */
    public CONST getCpuNr() {
        return cpuNr;
    }

    /**
     * Sets the number of CPU for specific virtual machine.
     */
    public void setCpuNr(CONST cpuNr) {
        this.cpuNr = cpuNr;
    }

    /**
     * Gets the number of memory for specific virtual machine.
     */
    public CONST getMemoryNr() {
        return memoryNr;
    }

    /**
     * Sets the number of memory for specific virtual machine.
     */
    public void setMemoryNr(CONST memoryNr) {
        this.memoryNr = memoryNr;
    }

    public CONST getHarddiskNr() {
        return harddiskNr;
    }

    public void setHarddiskNr(CONST harddiskNr) {
        this.harddiskNr = harddiskNr;
    }

    public CONST getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(CONST bandwidth) {
        this.bandwidth = bandwidth;
    }

}
