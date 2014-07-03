package es.bsc.vmmanagercore.monitoring;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import es.bsc.vmmanagercore.cloudmiddleware.JCloudsMiddleware;
import org.jclouds.openstack.nova.v2_0.domain.HostResourceUsage;
import org.jclouds.openstack.nova.v2_0.extensions.HostAdministrationApi;

/**
 * Status of a host of an OpenStack infrastructure.
 * 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public class HostInfoOpenStack extends HostInfo {

    private JCloudsMiddleware jcm;

    public HostInfoOpenStack(String name, JCloudsMiddleware jcm) {
        super(name);
        this.jcm = jcm;
        initTotalResources();
    }

    private void initTotalResources() {
        for (String zone: jcm.getZones()) {
            //get the host administration API
            Optional<? extends HostAdministrationApi> hostAdminApi =
                    jcm.getNovaApi().getHostAdministrationExtensionForZone(zone);

            //get the information about the host resources
            FluentIterable<? extends HostResourceUsage> hostResourcesInfo =
                    hostAdminApi.get().listResourceUsage(hostname);

            //get the information about the total resources of the host
            HostResourceUsage totalRes = hostResourcesInfo.get(0);

            //assign total CPU, RAM, and disk
            totalCpus = totalRes.getCpu();
            totalMemoryMb = totalRes.getMemoryMb();
            totalDiskGb = totalRes.getDiskGb();
        }
    }

    @Override
    public double getAssignedCpus() {
        int assignedCpus = 0;

        for (String zone: jcm.getZones()) {
            //get the host administration API
            Optional<? extends HostAdministrationApi> hostAdminApi =
                    jcm.getNovaApi().getHostAdministrationExtensionForZone(zone);

            //get the information about the host resources
            FluentIterable<? extends HostResourceUsage> hostResourcesInfo =
                    hostAdminApi.get().listResourceUsage(hostname);

            //get the assigned CPUs
            assignedCpus = hostResourcesInfo.get(1).getCpu();
        }

        //update the class attribute
        updateAssignedCpus(assignedCpus);

        return assignedCpus;
    }

    @Override
    public double getAssignedMemoryMb() {
        int assignedMemoryMb = 0;
        for (String zone: jcm.getZones()) {
            //get the host administration API
            Optional<? extends HostAdministrationApi> hostAdminApi =
                    jcm.getNovaApi().getHostAdministrationExtensionForZone(zone);

            //get the information about the host resources
            FluentIterable<? extends HostResourceUsage> hostResourcesInfo =
                    hostAdminApi.get().listResourceUsage(hostname);

            //get the assigned memory
            assignedMemoryMb = hostResourcesInfo.get(1).getMemoryMb();
        }

        //update the class attribute
        updateAssignedMemoryMb(assignedMemoryMb);

        return assignedMemoryMb;
    }

    @Override
    public double getAssignedDiskGb() {
        int assignedDiskGb = 0;
        for (String zone: jcm.getZones()) {
            //get the host administration API
            Optional<? extends HostAdministrationApi> hostAdminApi =
                    jcm.getNovaApi().getHostAdministrationExtensionForZone(zone);

            //get the information about the host resources
            FluentIterable<? extends HostResourceUsage> hostResourcesInfo =
                    hostAdminApi.get().listResourceUsage(hostname);

            //get the assigned disk
            assignedDiskGb = hostResourcesInfo.get(1).getDiskGb();
        }

        //update the class attribute
        updateAssignedDiskGb(assignedDiskGb);

        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    @Override
    public double getFreeCpus() {
        return totalCpus - getAssignedCpus() - reservedCpus;
    }

    /**
     * @return available memory of the host (in MB)
     */
    @Override
    public double getFreeMemoryMb() {
        return totalMemoryMb - getAssignedMemoryMb() - reservedMemoryMb;
    }

    /**
     * @return available disk space of the host (in GB)
     */
    @Override
    public double getFreeDiskGb() {
        return totalDiskGb - getAssignedDiskGb() - reservedDiskGb;
    }

}
