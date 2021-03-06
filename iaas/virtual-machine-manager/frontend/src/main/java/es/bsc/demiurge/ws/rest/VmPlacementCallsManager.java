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

package es.bsc.demiurge.ws.rest;

import com.google.gson.Gson;
import es.bsc.demiurge.core.models.scheduling.RecommendedPlanRequest;
import es.bsc.demiurge.core.models.scheduling.VmPlacement;
import es.bsc.demiurge.core.models.vms.Vm;
import es.bsc.demiurge.core.models.vms.VmRequirements;
import es.bsc.demiurge.core.cloudmiddleware.CloudMiddlewareException;
import es.bsc.demiurge.core.manager.VmManager;
import es.bsc.demiurge.core.models.scheduling.SelfAdaptationAction;
import es.bsc.demiurge.core.monitoring.hosts.Host;
import es.bsc.demiurge.core.monitoring.hosts.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the REST calls that are related with the placement of VMs.
 *
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class VmPlacementCallsManager {

    private VmManager vmManager;
    private Gson gson = new Gson();

    public VmPlacementCallsManager(VmManager vmManager) {
        this.vmManager = vmManager;
    }

    public String getConstructionHeuristics() {
        return gson.toJson(vmManager.getConstructionHeuristics());
    }

    public String getLocalSearchAlgorithms() {
        return gson.toJson(vmManager.getLocalSearchAlgorithms());
    }

    public String getRecommendedPlan(String recommendedPlanRequest) throws CloudMiddlewareException {
        SelfAdaptationAction action = new SelfAdaptationAction(false);
        return gson.toJson(vmManager.getRecommendedPlan(gson.fromJson(recommendedPlanRequest,
                RecommendedPlanRequest.class), action, new ArrayList<Vm>()));
    }

    public void executeDeploymentPlan(String deploymentPlan) throws CloudMiddlewareException {
        vmManager.executeDeploymentPlan(gson.fromJson(deploymentPlan, VmPlacement[].class));
    }
    
    public List<Slot> getSlots() throws CloudMiddlewareException {
        List<Slot> slots = new ArrayList<>();
        for(Host host : vmManager.getHosts()){
            slots.add(new Slot(host));
        }
        return slots;
    }
    
    public List<Slot> getSlots(String vmReq) throws CloudMiddlewareException {
        List<Slot> slots = new ArrayList<>();
        VmRequirements vm1 = gson.fromJson(vmReq, VmRequirements.class);
        for(Host host : vmManager.getHosts()){
            double freeCpus = host.getFreeCpus();
            double freeMem = host.getFreeMemoryMb();
            double freeDisk = host.getFreeDiskGb();
            
            //TODO: Add swap checks. Not implemented in Host object
            while(vm1.getRamMb() <= freeMem && vm1.getDiskGb() <= freeDisk && vm1.getCpus() <= freeCpus){
                slots.add(new Slot(host.getHostname(), vm1.getCpus(), vm1.getDiskGb(), vm1.getRamMb()));
                freeMem -= vm1.getRamMb();
                freeDisk -= vm1.getDiskGb();
                freeCpus -= vm1.getCpus();
            }
        }
        
        return slots;
    }
}