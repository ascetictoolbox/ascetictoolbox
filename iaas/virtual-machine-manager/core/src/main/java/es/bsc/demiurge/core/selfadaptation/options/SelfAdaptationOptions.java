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

package es.bsc.demiurge.core.selfadaptation.options;

/**
 * @author Mario Macias (github.com/mariomac), David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SelfAdaptationOptions {

    private final AfterVmDeploymentSelfAdaptationOps afterVmDeploymentSelfAdaptationOps;
    private final AfterVmDeleteSelfAdaptationOps afterVmDeleteSelfAdaptationOps;
    private final PeriodicSelfAdaptationOps periodicSelfAdaptationOps;
    private final OnDemandSelfAdaptationOps onDemandSelfAdaptationOps;

    public SelfAdaptationOptions(AfterVmDeploymentSelfAdaptationOps afterVmDeploymentSelfAdaptationOps,
                                 AfterVmDeleteSelfAdaptationOps afterVmDeleteSelfAdaptationOps,
                                 PeriodicSelfAdaptationOps periodicSelfAdaptationOps,
                                 OnDemandSelfAdaptationOps onDemandSelfAdaptationOps) {
        this.afterVmDeploymentSelfAdaptationOps = afterVmDeploymentSelfAdaptationOps;
        this.afterVmDeleteSelfAdaptationOps = afterVmDeleteSelfAdaptationOps;
        this.periodicSelfAdaptationOps = periodicSelfAdaptationOps;
        this.onDemandSelfAdaptationOps = onDemandSelfAdaptationOps;
    }

    public AfterVmDeploymentSelfAdaptationOps getAfterVmDeploymentSelfAdaptationOps() {
        return afterVmDeploymentSelfAdaptationOps;
    }

    public AfterVmDeleteSelfAdaptationOps getAfterVmDeleteSelfAdaptationOps() {
        return afterVmDeleteSelfAdaptationOps;
    }

    public PeriodicSelfAdaptationOps getPeriodicSelfAdaptationOps() {
        return periodicSelfAdaptationOps;
    }
    
    public OnDemandSelfAdaptationOps getOnDemandSelfAdaptationOps() {
        return onDemandSelfAdaptationOps;
    }

    @Override
    public String toString() {
        return "{" +
                "afterVmDeploymentSelfAdaptationOps=" + afterVmDeploymentSelfAdaptationOps +
                ", afterVmDeleteSelfAdaptationOps=" + afterVmDeleteSelfAdaptationOps +
                ", periodicSelfAdaptationOps=" + periodicSelfAdaptationOps +
                ", onDemandSelfAdaptationOps=" + onDemandSelfAdaptationOps +
                '}';
    }
}
