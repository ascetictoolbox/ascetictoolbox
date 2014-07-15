/**
 * Copyright 2014 University of Leeds
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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.HistoricUsageRecord;
import java.util.Collection;

/**
 * This interface for connecting to the background database with the aim of returning 
 * historical information and host calibration data.
 * @author Richard
 */
public interface DatabaseConnector {
    
    public Collection<Host> getHosts();
    public void setHosts(Collection<Host> hosts);
    public Collection<Host> getHostCalibrationData(Collection<Host> hosts);
    public Host getHostCalibrationData(Host host);
    public void setHostCalibrationData(Host host);
    public HistoricUsageRecord getVMHistoryData(VmDeployed VM);
    public void writeHostHistoricData(Host host, long time, double power, double energy);
    
}
