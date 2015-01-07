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

package es.bsc.vmplacement.domain;

import es.bsc.vmplacement.domain.comparators.HostStrengthComparator;
import es.bsc.vmplacement.domain.comparators.VmDifficultyComparator;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 * @author David Ortiz (david.ortiz@bsc.es)
 */
@PlanningEntity(difficultyComparatorClass = VmDifficultyComparator.class)
public class Vm extends AbstractPersistable {

    private int ncpus;
    private int ramMb;
    private int diskGb;
    private String appId = null;
    private Host host;
    private String alphaNumericId; /* This might be needed in some cases. For example, OpenStack uses alphanumeric
                                   IDs, and optaplanner needs an ID of type long. */

    public Vm() { }

    // I should apply the builder pattern here, but Optaplanner needs Vm() to clone

    public Vm(Long id, int ncpus, int ramMb, int diskGb) {
        this.id = id;
        this.ncpus = ncpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
    }

    public Vm(Long id, int ncpus, int ramMb, int diskGb, String appId) {
        this.id = id;
        this.ncpus = ncpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.appId = appId;
    }

    public Vm(Long id, int ncpus, int ramMb, int diskGb, String appId, String alphaNumericId) {
        this.id = id;
        this.ncpus = ncpus;
        this.ramMb = ramMb;
        this.diskGb = diskGb;
        this.appId = appId;
        this.alphaNumericId = alphaNumericId;
    }

    public int getNcpus() {
        return ncpus;
    }

    public int getRamMb() {
        return ramMb;
    }

    public int getDiskGb() {
        return diskGb;
    }

    public String getAppId() {
        return appId;
    }

    public String getAlphaNumericId() {
        return alphaNumericId;
    }

    @PlanningVariable(valueRangeProviderRefs = {"hostRange"}, strengthComparatorClass = HostStrengthComparator.class)
    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "VM - ID:" + id.toString() + ", cpus:" + ncpus + ", ram:" + ramMb + ", disk:" + diskGb
                + ", app:" + appId;
    }

}
