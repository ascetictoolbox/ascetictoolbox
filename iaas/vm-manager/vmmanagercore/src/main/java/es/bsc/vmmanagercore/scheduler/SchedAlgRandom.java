package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;

public class SchedAlgRandom implements SchedAlgorithm {

    public SchedAlgRandom() { }

    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm) {
        int randomHostIndex = (int)(Math.random()*hostsInfo.size());
        return hostsInfo.get(randomHostIndex).getHostname();
    }

}
