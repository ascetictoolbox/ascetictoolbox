/**
 * Copyright 2015 University of Leeds
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.energypredictor.workloadpredictor;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.DatabaseConnector;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.HostDataSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDiskImage;
import java.util.Collection;
import java.util.HashSet;

/**
 * This produces workload estimates for providing better energy estimations.
 *
 * @author Richard Kavanagh
 */
public abstract class AbstractWorkloadEstimator implements WorkloadEstimator {

    protected DatabaseConnector database = null;
    protected HostDataSource datasource = null;

    @Override
    public abstract double getCpuUtilisation(Host host, Collection<VM> virtualMachines);

    @Override
    public void setDataSource(HostDataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public void setDatabaseConnector(DatabaseConnector database) {
        this.database = database;
    }

    /**
     * This indicates if all VMs in the collection have application tags.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * application tags.
     * @return If the VMs all have application tags or not. True only if all
     * VMs have tags or the collection is empty.
     */
    public static boolean hasAppTags(Collection<VM> virtualMachines) {
        for (VM vm : virtualMachines) {
            if (vm.getApplicationTags().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * This gets a list of all application tags that a collection of VMs has
     * @param virtualMachines The virtual machines to get the application tags
     * from.
     * @return The set of all application tags known belonging to the VMs.
     */
    public static HashSet<String> getAppTags(Collection<VM> virtualMachines) {
        HashSet<String> answer = new HashSet<>();
        for (VM vm : virtualMachines) {
            answer.addAll(vm.getApplicationTags());
        }
        return answer;
    }    
    
    /**
     * This indicates if all VMs in the collection have disk references.
     *
     * @param virtualMachines The virtual machines to check to see if they have
     * disk references.
     * @return If the VMs all have disk references or not. True only if all
     * VMs have disk references or the collection is empty.
     */
    public static boolean hasDiskReferences(Collection<VM> virtualMachines) {
        for (VM vm : virtualMachines) {
            if (vm.getApplicationTags().isEmpty()) {
                return false;
            }
        }
        return true;
    } 
    
    /**
     * This gets a list of all disk references that a collection of VMs has
     * @param virtualMachines The virtual machines to get the disk references
     * from.
     * @return The set of all disk references known belonging to the VMs.
     */
    public static HashSet<VmDiskImage> getDiskReferences(Collection<VM> virtualMachines) {
        HashSet<VmDiskImage> answer = new HashSet<>();
        for (VM vm : virtualMachines) {
            answer.addAll(vm.getDiskImages());
        }
        return answer;
    }        

}
