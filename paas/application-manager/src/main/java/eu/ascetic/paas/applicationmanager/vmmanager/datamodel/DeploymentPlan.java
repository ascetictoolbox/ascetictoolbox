package eu.ascetic.paas.applicationmanager.vmmanager.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 /**
 * 
 * Copyright 2014 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class DeploymentPlan {
    private List<VmAssignmentToHost> vmsAssignationsToHosts = new ArrayList<>();

    public DeploymentPlan(List<VmAssignmentToHost> vmsAssignationsToHosts) {
        this.vmsAssignationsToHosts = vmsAssignationsToHosts;
    }

    public void addVmAssignmentToPlan(VmAssignmentToHost vmAssigmentToHost) {
        vmsAssignationsToHosts.add(vmAssigmentToHost);
    }

    public List<VmAssignmentToHost> getVmsAssignationsToHosts() {
        return new ArrayList<>(vmsAssignationsToHosts);
    }

    @Override
    public String toString() {
        String result = "";
        for (VmAssignmentToHost vmAssignmentToHost: vmsAssignationsToHosts) {
            result += vmAssignmentToHost.getVm().getName() + "-->" + vmAssignmentToHost.getHost().getHostname() + ", ";
        }
        return result;
    }

}


