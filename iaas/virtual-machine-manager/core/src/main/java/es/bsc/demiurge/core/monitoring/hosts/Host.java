/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.demiurge.core.monitoring.hosts;

import es.bsc.demiurge.core.models.hosts.HostPowerButtonAction;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.hosts.ServerLoad;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Status of a host of an infrastructure.
 * 
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 *
 */
public abstract class Host {

    protected String hostname;
    protected int totalCpus;
    protected double totalMemoryMb;
    protected double totalDiskGb;
    protected double assignedCpus;
    protected double assignedMemoryMb;
    protected double assignedDiskGb;

	// todo: quit this as a property. Use abstract getter
    protected double currentPower;
    
    protected AtomicBoolean turnedOff = new AtomicBoolean(false); // Several threads might try to turn on/off

    private Logger logger = LogManager.getLogger(Host.class);

    /**
     * Class constructor
     */
    public Host(String hostname) {
        this.hostname = hostname;
    }


    /**
     * Checks whether a host has enough available resources to host a VM.
     * @param cpus number of CPUs needed by the VM
     * @param memoryMb memory needed by the VM (in MB)
     * @param diskGb disk space needed by the VM (in GB)
     * @return Returns true if the host has enough available resources to host the VM.
     * Returns false if the host does not have enough available resources
     */
    public boolean hasEnoughResources(int cpus, int memoryMb, int diskGb) {
        return (getFreeCpus() >= cpus) && (getFreeMemoryMb() >= memoryMb) && (getFreeDiskGb() >= diskGb);
    }

    /**
     * Checks whether a specific host has enough resources to deploy a set of VMs.
     *
     * @param vms the list of VMs
     * @return true if the host has enough resources available, false otherwise
     */
    public boolean hasEnoughResourcesToDeployVms(List<Vm> vms) {
        int totalCpus, totalRamMb, totalDiskGb;
        totalCpus = totalRamMb = totalDiskGb = 0;
        for (Vm vm : vms) {
            totalCpus += vm.getCpus();
            totalRamMb += vm.getRamMb();
            totalDiskGb += vm.getDiskGb();
        }
        return hasEnoughResources(totalCpus, totalRamMb, totalDiskGb);
    }

    /**
     * @return host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return total number of CPUs of the host
     */
    public int getTotalCpus() {
        return totalCpus;
    }

    /**
     * @return total memory of the host (in MB)
     */
    public double getTotalMemoryMb() {
        return totalMemoryMb;
    }

    /**
     * @return total disk space of the host (in GB)
     */
    public double getTotalDiskGb() {
        return totalDiskGb;
    }

    /**
     * @return assigned CPUs of the host
     */
    public double getAssignedCpus() {
        return assignedCpus;
    }

    /**
     * @return assigned memory of the host (in MB)
     */
    public double getAssignedMemoryMb() {
        return assignedMemoryMb;
    }

    /**
     * @return assigned disk space of the host (in GB)
     */
    public double getAssignedDiskGb() {
        return assignedDiskGb;
    }

    /**
     * @return number of available CPUs of the host
     */
    public double getFreeCpus() {
        return totalCpus - assignedCpus;
    }

    /**
     * @return available memory of the host (in MB)
     */
    public double getFreeMemoryMb() {
        return totalMemoryMb - assignedMemoryMb;
    }

    /**
     * @return available disk space of the host (in GB)
     */
    public double getFreeDiskGb() {
        return totalDiskGb - assignedDiskGb;
    }

    
    /**
     * Returns the load that a host would have if a VM was deployed in it.
     *
     * @param vm the VM to deploy
     * @return the future load
     */
    public ServerLoad getFutureLoadIfVMDeployed(Vm vm) {
        return new ServerLoad(
                (getAssignedCpus() + vm.getCpus())/getTotalCpus(),
                (getAssignedMemoryMb() + vm.getRamMb())/getTotalMemoryMb(),
                (getAssignedDiskGb() + vm.getDiskGb())/getTotalDiskGb());
    }

    public void updateAssignedCpus(double assignedCpus) {
        this.assignedCpus = assignedCpus;
    }

    public void updateAssignedMemoryMb(double assignedMemoryMb) {
        this.assignedMemoryMb = assignedMemoryMb;
    }

    public void updateAssignedDiskGb(double assignedDiskGb) {
        this.assignedDiskGb = assignedDiskGb;
    }

    public ServerLoad getServerLoad() {
        return new ServerLoad(assignedCpus/totalCpus, assignedMemoryMb/totalMemoryMb, assignedDiskGb/totalDiskGb);
    }

    /**
     * Refreshes the metrics of the host (assigned CPUs, memory, etc.).
     */
    public abstract void refreshMonitoringInfo();

    /**
     * Presses the power button of the host.
     * If the host is turned on, then this function turns it off and vice versa. 
     */
    public void pressPowerButton() {
        // This is executed in a different thread. The reason is that we have defined a time delay between
        // the moment the power button is pressed and the moment the server is on or off.
        // In order to avoid blocking the main thread of execution, this is executed in a different thread.
        Thread thread;
        if (turnedOff.get()) {
            logger.debug("[VMM] Received request to turn on server " + hostname);
            thread = new Thread(new HostButtonPresserRunnable(this, HostPowerButtonAction.TURN_ON));
        }
        else {
            logger.debug("[VMM] Received request to turn off server " + hostname);
            thread = new Thread(new HostButtonPresserRunnable(this, HostPowerButtonAction.TURN_OFF));
        }
        thread.start();
    }
    
    /**
     * Turns on the host.
     */
    public void turnOn() {
		// This functionality is faked until a physical way to do this is available
        turnedOff.getAndSet(false);
        logger.debug("[VMM] Server " + hostname + " has been turned on");
    }

    /**
     * Turns off the host.
     */
    public void turnOff() {
		// This functionality is faked until a physical way to do this is available
        turnedOff.getAndSet(true);
        logger.debug("[VMM] Server " + hostname + " has been turned off");
    }

    /**
     * Returns whether the host is on.
     * @return True if the host is on, false otherwise
     */
    public boolean isOn() {
        return !turnedOff.get();
    }

	public double getCurrentPower() {
		return currentPower;
	}

	@Override
	public String toString() {
		return "Host{" +
				"hostname='" + hostname + '\'' +
				", totalCpus=" + totalCpus +
				", totalMemoryMb=" + totalMemoryMb +
				", totalDiskGb=" + totalDiskGb +
				", assignedCpus=" + assignedCpus +
				", assignedMemoryMb=" + assignedMemoryMb +
				", assignedDiskGb=" + assignedDiskGb +
				", currentPower=" + currentPower +
				", turnedOff=" + turnedOff +
				'}';
	}
}
