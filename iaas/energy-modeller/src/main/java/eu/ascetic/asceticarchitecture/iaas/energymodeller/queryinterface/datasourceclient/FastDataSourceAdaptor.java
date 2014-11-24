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

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.usage.CurrentUsageRecord;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This directly connects to a Zabbix database and scavenges the data required
 * directly, but also uses the Zabbix API in cases where the DB route fails. 
 * This thus eliminates some overheads of using the API but allows it as a 
 * fallback method.
 *
 * @author Richard
 */
public class FastDataSourceAdaptor implements HostDataSource {

    ZabbixDataSourceAdaptor zabbixAPI = new ZabbixDataSourceAdaptor();
    ZabbixDirectDbDataSourceAdaptor zabbixDbRoute = new ZabbixDirectDbDataSourceAdaptor();

    @Override
    public Host getHostByName(String hostname) {
        try {
            return zabbixDbRoute.getHostByName(hostname);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostByName(hostname);
        }
    }

    @Override
    public VmDeployed getVmByName(String name) {
        try {
            return zabbixDbRoute.getVmByName(name);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getVmByName(name);
        }
    }

    @Override
    public List<Host> getHostList() {
        try {
            return zabbixDbRoute.getHostList();
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostList();
        }
    }

    @Override
    public List<EnergyUsageSource> getHostAndVmList() {
        try {
            return zabbixDbRoute.getHostAndVmList();
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostAndVmList();
        }
    }

    @Override
    public List<VmDeployed> getVmList() {
        try {
            return zabbixDbRoute.getVmList();
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getVmList();
        }
    }

    @Override
    public HostMeasurement getHostData(Host host) {
        try {
            return zabbixDbRoute.getHostData(host);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostData(host);
        }
    }

    @Override
    public List<HostMeasurement> getHostData() {
        try {
            return zabbixDbRoute.getHostData();
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostData();
        }
    }

    @Override
    public List<HostMeasurement> getHostData(List<Host> hostList) {
        try {
            return zabbixDbRoute.getHostData(hostList);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getHostData(hostList);
        }
    }

    @Override
    public VmMeasurement getVmData(VmDeployed vm) {
        try {
            return zabbixDbRoute.getVmData(vm);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getVmData(vm);
        }
    }

    @Override
    public List<VmMeasurement> getVmData() {
        try {
            return zabbixDbRoute.getVmData();
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getVmData();
        }
    }

    @Override
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList) {
        try {
            return zabbixDbRoute.getVmData(vmList);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getVmData(vmList);
        }
    }

    @Override
    public CurrentUsageRecord getCurrentEnergyUsage(Host host) {
        try {
            return zabbixDbRoute.getCurrentEnergyUsage(host);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getCurrentEnergyUsage(host);
        }
    }

    @Override
    public double getLowestHostPowerUsage(Host host) {
        try {
            return zabbixDbRoute.getLowestHostPowerUsage(host);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getLowestHostPowerUsage(host);
        }
    }

    @Override
    public double getHighestHostPowerUsage(Host host) {
        try {
            return zabbixDbRoute.getHighestHostPowerUsage(host);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getLowestHostPowerUsage(host);
        }
    }

    @Override
    public double getCpuUtilisation(Host host, int durationSeconds) {
        try {
            return zabbixDbRoute.getCpuUtilisation(host, durationSeconds);
        } catch (Exception ex) {
            Logger.getLogger(FastDataSourceAdaptor.class.getName()).log(Level.INFO, "Performing fallback to Zabbix API", ex);
            return zabbixAPI.getCpuUtilisation(host, durationSeconds);
        }
    }

}
