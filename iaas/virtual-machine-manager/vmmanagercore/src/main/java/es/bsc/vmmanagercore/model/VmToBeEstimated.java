package es.bsc.vmmanagercore.model;

/**
 *
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class VmToBeEstimated {

    private String id;
    private int vcpus;
    private int cpuFreq;
    private int ramMb;
    private int diskGb;

    Vm convertToVm() {
        return new Vm(id, "", vcpus, ramMb, diskGb, "", "");
    }

}
