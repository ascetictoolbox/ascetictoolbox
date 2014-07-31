package es.bsc.vmmanagercore.model;

import java.util.Date;

/**
 * VM that has been deployed in the cloud middleware.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmDeployed extends Vm {

    private String id;
    private String ipAddress;
    private String state;
    private Date created;
    private String hostName;

    public VmDeployed(String name, String image, int cpus, int ramMb,
            int diskGb, String initScript, String applicationId, String id,
            String ipAddress, String state, Date created, String hostName) {
        super(name, image, cpus, ramMb, diskGb, initScript, applicationId);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = new Date(created.getTime());
        this.hostName = hostName;
    }

    public String getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getState() {
        return state;
    }

    public Date getCreated() {
        return new Date(created.getTime());
    }

    public String getHostName() {
        return hostName;
    }

}
