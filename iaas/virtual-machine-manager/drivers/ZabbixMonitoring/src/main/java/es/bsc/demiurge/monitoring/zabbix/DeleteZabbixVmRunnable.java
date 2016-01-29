package es.bsc.demiurge.monitoring.zabbix;

/**
 * Runnable to delete a VM in Zabbix. A client who makes a delete VM request should not wait
 * for Zabbix to delete the VM from its DB. This is why we execute the delete action in a separated thread.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeleteZabbixVmRunnable implements Runnable {

    private final String vmId;
    private final String hostname;

    public DeleteZabbixVmRunnable(String vmId, String hostname) {
        this.vmId = vmId;
        this.hostname = hostname;
    }

    @Override
    public void run() {
        ZabbixConnector.getZabbixClient().deleteVM(vmId);
    }

}
