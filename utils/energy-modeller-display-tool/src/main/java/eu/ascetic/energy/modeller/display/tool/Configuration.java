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
package eu.ascetic.energy.modeller.display.tool;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * This gets the configuration data for the energy modellers database. If the
 * settings file is absent a fresh copy will be created with default values.
 *
 * @author Richard
 */
public class Configuration {

    /**
     * The url to contact the database.
     */
    public static int lowerMain = 0;
    public static int upperMain = 500;
    public static int lowerVMGraph = 0;
    public static int upperVMGraph = 500;
    
    private static final String CONFIG_FILE = "display.properties";

    static {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            lowerMain = config.getInt("iaas.energy.modeller.graph.main.lower", lowerMain);
            config.setProperty("iaas.energy.modeller.graph.main.lower", lowerMain);
            upperMain = config.getInt("iaas.energy.modeller.graph.main.upper", upperMain);
            config.setProperty("iaas.energy.modeller.graph.main.upper", upperMain);
            lowerVMGraph = config.getInt("iaas.energy.modeller.graph.vm.lower", lowerVMGraph);
            config.setProperty("iaas.energy.modeller.graph.vm.lower", lowerVMGraph);
            upperVMGraph = config.getInt("iaas.energy.modeller.graph.vm.upper", upperVMGraph);
            config.setProperty("iaas.energy.modeller.graph.vm.upper", upperVMGraph);
        } catch (ConfigurationException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }
    }

}
