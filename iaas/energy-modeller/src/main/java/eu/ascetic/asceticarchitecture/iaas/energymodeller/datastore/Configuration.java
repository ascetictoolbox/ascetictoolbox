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
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class Configuration {

    /**
     * The url to contact the database.
     */
    public static String databaseURL = "jdbc:mysql://10.4.0.15:3306/ascetic-em"; //"jdbc:mysql://iaas-vm-dev:3306/ascetic-em";
    /**
     * The driver to be used to contact the database.
     */
    public static String databaseDriver = "com.mysql.jdbc.Driver";
    /**
     * The user details to contact the database.
     */
    public static String databaseUser = "ascetic-em";
    /**
     * The user's password to contact the database.
     */
    public static String databasePassword = "em";
    private static final String CONFIG_FILE = "energymodeller.properties";

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
            databaseURL = config.getString("iaas.energy.modeller.db.url", databaseURL);
            config.setProperty("iaas.energy.modeller.db.url", databaseURL);
            databaseDriver = config.getString("iaas.energy.modeller.db.driver", databaseDriver);
            config.setProperty("iaas.energy.modeller.db.driver", databaseDriver);
            databasePassword = config.getString("iaas.energy.modeller.db.password", databasePassword);
            config.setProperty("iaas.energy.modeller.db.password", databasePassword);
            databaseUser = config.getString("iaas.energy.modeller.db.user", databaseUser);
            config.setProperty("iaas.energy.modeller.db.user", databaseUser);
        } catch (ConfigurationException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "Error loading the configuration of the IaaS energy modeller", ex);
        }
    }

}
