package es.bsc.vmmanagercore.model;

import java.util.Date;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

/**
 * 
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmDeployed extends Vm {

    private String id;
    private String ipAddress;
    private String state;
    private Date created;

    public VmDeployed(String name, String image, int cpus, int ramMb,
            int diskGb, String initScript, String applicationId, String id,
            String ipAddress, String state, Date created) {
        super(name, image, cpus, ramMb, diskGb, initScript, applicationId);
        this.id = id;
        this.ipAddress = ipAddress;
        this.state = state;
        this.created = created;
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
        return created;
    }

}
