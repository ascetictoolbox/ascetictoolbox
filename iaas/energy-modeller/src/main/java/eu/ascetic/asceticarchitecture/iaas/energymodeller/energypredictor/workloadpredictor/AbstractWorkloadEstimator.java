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
 * This produces workload estimates for the purpose of providing better 
 * energy estimations.
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

}
