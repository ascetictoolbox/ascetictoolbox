package es.bsc.vmmanagercore.scheduler;

import es.bsc.vmmanagercore.model.Vm;
import es.bsc.vmmanagercore.monitoring.HostInfo;

import java.util.ArrayList;

/**
 *  @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public interface SchedAlgorithm {

    public String chooseHost(ArrayList<HostInfo> hostsInfo, Vm vm);

}