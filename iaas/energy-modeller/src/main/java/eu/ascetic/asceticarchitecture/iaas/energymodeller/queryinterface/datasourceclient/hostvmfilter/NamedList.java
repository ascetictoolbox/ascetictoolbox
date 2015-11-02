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
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This is a way of filtering deciding between if a Zabbix Host is a VM or a
 * real physical host. It is based upon the host's name following belonging to a
 * set of known physical hosts.
 *
 * @author Richard Kavanagh
 */
public class NamedList implements ZabbixHostVMFilter {

    public static final String CONFIG_FILE = "energy-modeller-zabbix.properties";

    private String namedSet = "asok09,asok10,asok11,asok12";
    private HashSet<String> hostNames = new HashSet<>();

    /**
     * This creates a name filter that checks to see if the start of a host name
     * matches particular criteria or not. if it does then it will indicate
     * accordingly that the Zabbix host is a Energy modeller host or not.
     */
    public NamedList() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            namedSet = config.getString("iaas.energy.modeller.filter.names", namedSet);
            config.setProperty("iaas.energy.modeller.filter.names", namedSet);
            hostNames.addAll(Arrays.asList(namedSet.split(",")));
        } catch (ConfigurationException ex) {
            Logger.getLogger(NameBeginsFilter.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }
    }

    @Override
    public boolean isHost(Host host) {
        return hostNames.contains(host.getHost());
    }

}
