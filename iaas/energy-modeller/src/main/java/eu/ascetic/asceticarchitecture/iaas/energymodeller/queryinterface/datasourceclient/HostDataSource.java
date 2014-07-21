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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import java.util.List;

/**
 * This is the interface for all data sources that the Energy modeller uses, that 
 * concerns Hosts.
 * @author Richard
 */
public interface HostDataSource {
    
    public Host getHostByName(String hostname);
    public List<Host> getHostList();
    public List<VmDeployed> getVmList();
    public HostMeasurement getHostData(Host host);    
    public List<HostMeasurement> getHostData();
    public List<HostMeasurement> getHostData(List<Host> hostList);
    public VmMeasurement getVmData(VmDeployed vm); 
    public List<VmMeasurement> getVmData();
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList);    
    public CurrentUsageRecord getCurrentEnergyUsage(Host host);
    public double getLowestHostPowerUsage(Host host);
}
