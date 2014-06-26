package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.List;

/**
 * Energy-aware scheduling algorithm.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class SchedAlgEnergyAware implements SchedAlgorithm {

    @Override
    public String chooseHost(List<HostInfo> hostsInfo, Vm vm) {
        //TODO write here the call to the Energy Modeller
        return null;
    }

}
