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

package es.bsc.vmplacement.domain.comparators;

import es.bsc.vmplacement.domain.Host;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class compares the "strength" of two hosts. Hosts with more resources are considered to be "stronger"
 * because they are more likely to meet the requirements needed to deploy a VM.
 * Comparing the "strength" of two hosts is needed to apply some construction heuristic algorithms like the best fit
 * (aka weakest fit).
 *
 * @author David Ortiz (david.ortiz@bsc.es)
 */
public class HostStrengthComparator implements Comparator<Host>, Serializable {

    /**
     * This function compares the "strength" of two hosts.
     *
     * @param host1 a host
     * @param host2 a host
     * @return a negative number if host1 is weaker than host2, a positive number if host1 is stronger than host2, and
     * 0 if they are equal
     */
    @Override
    public int compare(Host host1, Host host2) {
        return Double.compare(strength(host1), strength(host2));
    }

    /**
     * This function calculate the strength of a host.
     * This is the formula used to calculate the difficulty: host.cpus * (host.ramMb/1000) * (host.diskGb/100).
     * The memory and the disk capacity are divided in the formula because it would not be fair to give the same
     * weight to 1 CPU than to 1 MB or RAM.
     */
    private double strength(Host host) {
        return host.getNcpus()*(host.getRamMb()/1000.0)*(host.getDiskGb()/100.0);
    }

}
