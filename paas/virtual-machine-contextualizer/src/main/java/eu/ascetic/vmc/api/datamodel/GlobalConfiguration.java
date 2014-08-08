/**
 *  Copyright 2013 University of Leeds
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.ascetic.vmc.api.datamodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Class to store the configuration details of the VMC
 * 
 * TODO: Check for OS and dependency requirements
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class GlobalConfiguration {

    private static final String REPOSITORY_PROPERTY = "repository";

    protected static final Logger LOGGER = Logger
            .getLogger(GlobalConfiguration.class);

    private boolean defaultValues;
    private String configFilePath;

    // Stored in properties file
    private String installDirectory;
    private String repository;
    private Boolean addRecontextFiles;

    // Generated from properties file
    private String contextDataDirectory;
    private String agentsDirectory;

    /**
     * Constructor for setting configuration variables.
     * 
     * @param configFilePath
     *            The file path to the configuration file (FIXME: Not the file
     *            name)
     * @throws Exception
     *             If config file is not present or unreadable.
     */
    public GlobalConfiguration(String configFilePath) throws Exception {

        this.configFilePath = configFilePath;
        LOGGER.info("Using configFilePath: '" + configFilePath + "'");
        defaultValues = false;

        config();
    }

    /**
     * Constructor used for testing purposes, using default configuration values
     * 
     */
    public GlobalConfiguration() {

        LOGGER.info("Using default config values for testing purposes...");
        defaultValues = true;

        try {
            config();
        } catch (Exception e) {
            // Do nothing as config file is not loaded here...
        }
    }

    /**
     * Gets the variables from the config properties file.
     * 
     * @throws Exception
     *             Thrown if config file is not present or unreadable.
     */
    private void config() throws Exception {

        // Read properties file.
        Properties properties = new Properties();

        if (defaultValues) {
            // TODO: Changes these paths to OS specific temp folders
            String systemTempDir = System.getProperty("java.io.tmpdir");
            String vmcTemp = systemTempDir + "vmc";
            properties.setProperty("installDirectory", vmcTemp + File.separator
                    + "runtime");
            new File(properties.getProperty("installDirectory")).mkdirs();
            properties.setProperty(REPOSITORY_PROPERTY, vmcTemp
                    + File.separator + REPOSITORY_PROPERTY);
            new File(properties.getProperty(REPOSITORY_PROPERTY)).mkdirs();
            properties.setProperty("addRecontextFiles", "false");
        } else {
            try {
                properties.load(new FileInputStream(this.configFilePath
                        + File.separator + "config.properties"));
            } catch (IOException e) {
                LOGGER.error(
                        "The config.properties file does not exist at location: "
                                + this.configFilePath + File.separator
                                + "config.properties", e);
                throw new Exception(
                        "The config.properties file does not exist!", e);
            }
        }

        this.installDirectory = null;
        installDirectory = properties.getProperty("installDirectory");
        LOGGER.info("Using install dir: '" + installDirectory + "'");

        this.repository = null;
        repository = properties.getProperty(REPOSITORY_PROPERTY);
        LOGGER.info("Using repository dir: '" + repository + "'");

        this.addRecontextFiles = false;
        addRecontextFiles = Boolean.parseBoolean(properties
                .getProperty("addRecontextFiles"));
        LOGGER.info("Using addRecontextFiles value: '"
                + addRecontextFiles.toString() + "'");

        this.contextDataDirectory = null;
        contextDataDirectory = installDirectory + File.separator + "context";

        this.agentsDirectory = null;
        agentsDirectory = installDirectory + File.separator + "agents";
    }

    /**
     * @return the installDirectory
     */
    public String getInstallDirectory() {
        return installDirectory;
    }

    /**
     * @param installDirectory
     *            the installDirectory to set
     */
    public void setInstallDirectory(String installDirectory) {
        this.installDirectory = installDirectory;
    }

    /**
     * @return the repository
     */
    public String getRepository() {
        return repository;
    }

    /**
     * @param repository
     *            the repository to set
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * @return the addRecontextFiles
     */
    public Boolean getAddRecontextFiles() {
        return addRecontextFiles;
    }

    /**
     * @param addRecontextFiles
     *            the addRecontextFiles to set
     */
    public void setAddRecontextFiles(Boolean addRecontextFiles) {
        this.addRecontextFiles = addRecontextFiles;
    }

    /**
     * @return the contextDataDirectory
     */
    public String getContextDataDirectory() {
        return contextDataDirectory;
    }

    /**
     * @param contextDataDirectory
     *            the contextDataDirectory to set
     */
    public void setContextDataDirectory(String contextDataDirectory) {
        this.contextDataDirectory = contextDataDirectory;
    }

    /**
     * @return the agentsDirectory
     */
    public String getAgentsDirectory() {
        return agentsDirectory;
    }

    /**
     * @param agentsDirectory
     *            the agentsDirectory to set
     */
    public void setAgentsDirectory(String agentsDirectory) {
        this.agentsDirectory = agentsDirectory;
    }

    /**
     * @return the defaultValues
     */
    public boolean isDefaultValues() {
        return defaultValues;
    }

    /**
     * @return the configFilePath
     */
    public String getConfigFilePath() {
        return configFilePath;
    }
}
