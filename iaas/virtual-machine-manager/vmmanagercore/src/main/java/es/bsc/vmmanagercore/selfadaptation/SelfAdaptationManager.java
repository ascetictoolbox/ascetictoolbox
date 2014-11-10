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

package es.bsc.vmmanagercore.selfadaptation;

import es.bsc.vmmanagercore.db.VmManagerDb;
import es.bsc.vmmanagercore.db.VmManagerDbFactory;
import es.bsc.vmmanagercore.manager.VmManager;
import es.bsc.vmmanagercore.model.scheduling.ConstructionHeuristic;
import es.bsc.vmmanagercore.model.scheduling.RecommendedPlanRequest;

/**
 * Self-adaptation Manager.
 *
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class SelfAdaptationManager {

    private VmManager vmManager;
    private VmManagerDb db;

    /**
     * Class constructor.
     *
     * @param vmManager instance of the VMM
     * @param dbName The name of the DB used by the VMM
     */
    public SelfAdaptationManager(VmManager vmManager, String dbName) {
        this.vmManager = vmManager;
        db = VmManagerDbFactory.getDb(dbName);
    }

    /**
     * This function updates the configuration options in the DB.
     *
     * @param selfAdaptationOptions the options
     */
    public void saveSelfAdaptationOptions(SelfAdaptationOptions selfAdaptationOptions) {
        db.saveSelfAdaptationOptions(selfAdaptationOptions);
    }

    /**
     * Returns the self-adaptation options for the self-adaptation capabilities of the VMM.
     * If the system does not have any self-adaptation options save, it returns a set of options that have
     * been defined as the default ones.
     *
     * @return the options
     */
    public SelfAdaptationOptions getSelfAdaptationOptions() {
        if (db.getSelfAdaptationOptions() == null) {
            return getDefaultSelfAdaptationOptions();
        }
        return db.getSelfAdaptationOptions();
    }

    /**
     * Applies the self-adaptation configured to take place after a VM deployment.
     */
    public void applyAfterVmDeploymentSelfAdaptation() {
        //TODO
    }

    /**
     * Applies the self-adaptation configured to take place after deleting a VM.
     */
    public void applyAfterVmDeleteSelfAdaptation() {
        AfterVmDeleteSelfAdaptationOps options = getSelfAdaptationOptions().getAfterVmDeleteSelfAdaptationOps();

        if (options.getLocalSearchAlgorithm() != null && options.getMaxExecTimeSeconds() > 0) {
            // The construction heuristic is set to first fit, but anyone could be selected because in this case,
            // all the VMs are already assigned to a host. Therefore, it is not needed to apply a construction heuristic
            RecommendedPlanRequest recommendedPlanRequest =
                    new RecommendedPlanRequest(options.getMaxExecTimeSeconds(),
                            "FIRST_FIT",
                            options.getLocalSearchAlgorithm());

            vmManager.executeDeploymentPlan(vmManager.getRecommendedPlan(recommendedPlanRequest).getVMPlacements());
        }
    }

    /**
     * Applies the self-adaptation configured to take place periodically.
     */
    public void applyPeriodicSelfAdaptation() {
        //TODO
    }

    /**
     * Returns the default self-adaptation options.
     *
     * @return the default self-adaptation options
     */
    private SelfAdaptationOptions getDefaultSelfAdaptationOptions() {
        return new SelfAdaptationOptions(
                new AfterVmDeploymentSelfAdaptationOps(new ConstructionHeuristic("FIRST_FIT"), null, 0),
                new AfterVmDeleteSelfAdaptationOps(null, 0),
                new PeriodicSelfAdaptationOps(null, 0, 0));
    }

}
