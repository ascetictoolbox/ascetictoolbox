package es.bsc.vmmanagercore.scheduler;

import java.util.ArrayList;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

public class SchedAlgRandom implements SchedAlgorithm {

    public SchedAlgRandom() { }

    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm) {
        int randomHostIndex = (int)(Math.random()*hostsInfo.size());
        return hostsInfo.get(randomHostIndex).getHostname();
    }

}
