package es.bsc.vmmanagercore.rest;

import com.google.gson.Gson;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.monitoring.Host;

/**
 * This class implements the REST calls that are related with the nodes of the infrastructure.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class NodeCallsManager {

    private Gson gson = new Gson();
    private VmManager vmManager;

    /**
     * Class constructor.
     */
    public NodeCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    public String getNodes() {
        // TODO Refactor this ugly hack
        String result = "{\"nodes\":[";
        for (int i = 0; i < vmManager.getHosts().size(); ++i) {
            result = result.concat(gson.toJson(vmManager.getHosts().get(i), Host.class));
            if (i != vmManager.getHosts().size() -1) {
                result = result.concat(",");
            }
        }
        return result.concat("]}");
    }

    public String getVMsDeployedInNode(String hostname) {
        return gson.toJson(vmManager.getHost(hostname), Host.class);
    }

}
