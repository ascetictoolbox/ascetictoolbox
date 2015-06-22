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
import java.util.Collection;

/**
 * This produces workload estimates for providing better energy estimations.
 *
 * @author Richard Kavanagh
 */
public interface WorkloadEstimator {

    /**
     * This estimates a physical hosts CPU utilisation. It is based upon which
     * VMs are expected to be deployed/are currently deployed.
     * @param host The physical host to get the workload estimation for.
     * @param virtualMachines The virtual machines that induce the workload.
     * @return The estimated CPU utilisation of the physical host.
     */
    public double getCpuUtilisation(Host host, Collection<VM> virtualMachines);

    /**
     * This sets the data source that is to be used for querying current data. 
     * @param datasource The data source to use for current information
     */
    public void setDataSource(HostDataSource datasource);

    /**
     * This sets the database that is to be used for querying historical data.
     * @param database The database to use
     */
    public void setDatabaseConnector(DatabaseConnector database);
    
    /**
     * This indicates if the predictor requires VM information or not in order
     * to make its prediction.
     * @return If the predictor requires VM information to make a prediction. 
     * True only if this is the case, otherwise the predictor will utilise 
     * host only information.
     */
    public boolean requiresVMInformation();

}
