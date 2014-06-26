/**
 *  Copyright 2014 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.input;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VM;
import java.util.*;

/**
 * This represents a projected workload on a VM.  
 * @deprecated Not needed in the 1st year. 100% Load only should be considered.
 * @author Richard Kavanagh
 */
public class VMProjectedWorkload {

    private VM vm;
    private TreeSet<VMProjectedWorkloadElement> workload = new TreeSet<>();

    /**
     * This creates an instance of a projected workload for a given VM
     * @param vm The VM to create the projected workload for
     */
    public VMProjectedWorkload(VM vm) {
        this.vm = vm;
    }

    /**
     * This returns the VM associated with this workload.
     * @return 
     */
    public VM getVm() {
        return vm;
    }
    
    /**
     * This adds a workload element to the overall workload
     * @param element a part of the workload.
     * @return 
     */
    public boolean add(VMProjectedWorkloadElement element) {
        return workload.add(element);
    }
    
    /**
     * This adds a collection of workload elements to the overall workload
     * @param collection
     * @return 
     */
    public boolean addAll(Collection<VMProjectedWorkloadElement> collection) {
        return workload.addAll(collection);
    }
    
    /**
     * This removes a workload element to the overall workload
     * @param element
     * @return 
     */
    public boolean remove(VMProjectedWorkloadElement element) {
        return workload.remove(element);
    } 
    
    /**
     * This removes a collection of workload elements from the overall workload
     * @param collection
     * @return 
     */
    public boolean removeAll(Collection<VMProjectedWorkloadElement> collection) {
        return workload.removeAll(collection);
    }    

    /**
     * This returns a projected workload that indicates the workload after a 
     * specified time.
     *
     * @param afterTime The time after which workload elements will make up the
     * new workload object.
     * @return The workload after the specified point in time
     */
    public VMProjectedWorkload getAllTasksAfter(Calendar afterTime) {
        VMProjectedWorkload result = new VMProjectedWorkload(vm);
        for (VMProjectedWorkloadElement action : workload) {
            if (action.getStartTime().after(afterTime)) {
                result.add(action);
            }
        }
        return result;
    }
    
    /**
     * This returns the start time for the projected workload.
     *
     * @return The start time for the projected workload.
     */
    public Calendar getStartTime() {
        if (workload.isEmpty()) {
            return null;
        } else {
            return workload.first().getStartTime();
        }
    }       
    
    /**
     * This returns the end time for the projected workload.
     *
     * @return The end time for the projected workload.
     */
    public Calendar getEndTime() {
        if (workload.isEmpty()) {
            return null;
        } else {
            return workload.last().getEndTime();
        }
    } 
    
}