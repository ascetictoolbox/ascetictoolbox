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

/**
 * Status of a host of an infrastructure.
 * 
 * @author Raimon Bosch (raimon.bosch@bsc.es)
 *
 */
public class Slot {
    
    private String hostname;
    private double freeMemoryMb;
    private double freeCpus;
    private double freeDiskGb;
    
    public Slot(Host host){
        this.hostname = host.getHostname();
        this.freeCpus = host.getFreeCpus();
        this.freeDiskGb = host.getFreeDiskGb();
        this.freeMemoryMb = host.getFreeMemoryMb();
    }

    /**
     * @return the freeMemoryMb
     */
    public double getFreeMemoryMb() {
        return freeMemoryMb;
    }

    /**
     * @param freeMemoryMb the freeMemoryMb to set
     */
    public void setFreeMemoryMb(double freeMemoryMb) {
        this.freeMemoryMb = freeMemoryMb;
    }

    /**
     * @return the freeCpus
     */
    public double getFreeCpus() {
        return freeCpus;
    }

    /**
     * @param freeCpus the freeCpus to set
     */
    public void setFreeCpus(double freeCpus) {
        this.freeCpus = freeCpus;
    }

    /**
     * @return the freeDiskGb
     */
    public double getFreeDiskGb() {
        return freeDiskGb;
    }

    /**
     * @param freeDiskGb the freeDiskGb to set
     */
    public void setFreeDiskGb(double freeDiskGb) {
        this.freeDiskGb = freeDiskGb;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
