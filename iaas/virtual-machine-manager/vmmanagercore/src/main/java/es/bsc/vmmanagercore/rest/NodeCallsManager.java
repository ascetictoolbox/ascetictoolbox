package es.bsc.vmmanagercore.rest;

import es.bsc.vmmanagercore.manager.VmManager;

/**
 * This class implements the REST calls that are related with the nodes of the infrastructure.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class NodeCallsManager {

    private VmManager vmManager;

    public NodeCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    public String getNodes() {
        //TODO
        return null;
    }

    public String getVMsDeployedInNode(String hostname) {
        //TODO
        return null;
    }

}
