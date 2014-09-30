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
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;

/**
 * The aim of this is to indicate if a Zabbix Host is a VM or a Host machine.
 * @author Richard
 */
public interface ZabbixHostVMFilter {
    
    /**
     * Indicates if the Zabbix Host, is a Energy modeller host or not. The only
     * other option is for it to be a Energy modeller Virtual machine.
     * @param host The Zabbix host.
     * @return If it is a host or not.
     */
    public boolean isHost(Host host);
    
}
