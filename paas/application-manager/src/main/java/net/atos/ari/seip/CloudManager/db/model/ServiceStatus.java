package net.atos.ari.seip.CloudManager.db.model;
// Generated 17-Jun-2014 11:35:53 by Hibernate Tools 3.2.2.GA


import java.util.Date;

/**
 * ServiceStatus generated by hbm2java
 */
public class ServiceStatus  implements java.io.Serializable {


     private Integer idserviceStatus;
     private ServiceInfo serviceInfo;
     private String serviceStatus;
     private Date time;

    public ServiceStatus() {
    }

	
    public ServiceStatus(ServiceInfo serviceInfo, String serviceStatus) {
        this.serviceInfo = serviceInfo;
        this.serviceStatus = serviceStatus;
    }
    public ServiceStatus(ServiceInfo serviceInfo, String serviceStatus, Date time) {
       this.serviceInfo = serviceInfo;
       this.serviceStatus = serviceStatus;
       this.time = time;
    }
   
    public Integer getIdserviceStatus() {
        return this.idserviceStatus;
    }
    
    public void setIdserviceStatus(Integer idserviceStatus) {
        this.idserviceStatus = idserviceStatus;
    }
    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }
    
    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    public String getServiceStatus() {
        return this.serviceStatus;
    }
    
    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    public Date getTime() {
        return this.time;
    }
    
    public void setTime(Date time) {
        this.time = time;
    }




}


