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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVcpus() {
        return vcpus;
    }

    public void setVcpus(int vcpus) {
        this.vcpus = vcpus;
    }

    public int getCpuFreq() {
        return cpuFreq;
    }

    public void setCpuFreq(int cpuFreq) {
        this.cpuFreq = cpuFreq;
    }

    public int getRamMb() {
        return ramMb;
    }

    public void setRamMb(int ramMb) {
        this.ramMb = ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public void setDiskGb(int diskGb) {
        this.diskGb = diskGb;
    }

    public Vm toVm() {
        return new Vm(id, "", vcpus, ramMb, diskGb, "", "");
    }

}
