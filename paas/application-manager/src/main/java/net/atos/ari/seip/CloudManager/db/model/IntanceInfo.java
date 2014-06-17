package net.atos.ari.seip.CloudManager.db.model;
// Generated 17-Jun-2014 11:35:53 by Hibernate Tools 3.2.2.GA


import java.util.HashSet;
import java.util.Set;

/**
 * IntanceInfo generated by hbm2java
 */
public class IntanceInfo  implements java.io.Serializable {


     private Integer idintanceInfo;
     private ServiceInfo serviceInfo;
     private String instanceId;
     private Set instanceNetworks = new HashSet(0);
     private Set instanceStatuses = new HashSet(0);

    public IntanceInfo() {
    }

	
    public IntanceInfo(String instanceId) {
        this.instanceId = instanceId;
    }
    public IntanceInfo(ServiceInfo serviceInfo, String instanceId, Set instanceNetworks, Set instanceStatuses) {
       this.serviceInfo = serviceInfo;
       this.instanceId = instanceId;
       this.instanceNetworks = instanceNetworks;
       this.instanceStatuses = instanceStatuses;
    }
   
    public Integer getIdintanceInfo() {
        return this.idintanceInfo;
    }
    
    public void setIdintanceInfo(Integer idintanceInfo) {
        this.idintanceInfo = idintanceInfo;
    }
    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }
    
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    public String getInstanceId() {
        return this.instanceId;
    }
    
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
    public Set getInstanceNetworks() {
        return this.instanceNetworks;
    }
    
    public void setInstanceNetworks(Set instanceNetworks) {
        this.instanceNetworks = instanceNetworks;
    }
    public Set getInstanceStatuses() {
        return this.instanceStatuses;
    }
    
    public void setInstanceStatuses(Set instanceStatuses) {
        this.instanceStatuses = instanceStatuses;
    }




}


