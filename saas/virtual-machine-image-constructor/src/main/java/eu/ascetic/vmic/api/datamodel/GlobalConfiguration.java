/**
 *  Copyright 2014 University of Leeds
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
package eu.ascetic.vmic.api.datamodel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Class to store the configuration details of the VMIC
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class GlobalConfiguration {

    protected static final Logger LOGGER = Logger
            .getLogger(GlobalConfiguration.class);
    
    private static final String REPOSITORY_PROPERTY = "repository";
    
    private static final String HOST_ADDRESS_PROPERTY_KEY = "hostAddress";
    private static final String REPOSITORY_PROPERTY_KEY = "repositoryPath";
    private static final String RSYNC_PROPERTY_KEY = "rsyncPath";
    private static final String SSH_PROPERTY_KEY = "sshPath";
    private static final String SSH_KEY_PROPERTY_KEY = "sshKeyPath";
    private static final String SSH_USER_PROPERTY_KEY = "sshUser";

    private boolean defaultValues;
    private String configPropertiesFileUri;

    private String hostAddress;
    private String repositoryPath;
    private String rsyncPath;

    private String sshPath;
    private String sshKeyPath;
    private String sshUser;

    /**
     * Constructor for setting configuration variables.
     * 
     * @param configPropertiesFileUri
     *            The file path to the configuration file (including file name)
     * @throws Exception
     *             If config file is not present or unreadable
     */
    public GlobalConfiguration(String configPropertiesFileUri) throws Exception {

        this.configPropertiesFileUri = configPropertiesFileUri;
        LOGGER.info("Using configPropertiesFileUri: '"
                + configPropertiesFileUri + "'");
        defaultValues = false;

        config();
    }

    /**
     * Constructor used for testing purposes, using default configuration
     * values. Can alternatively be used to set configuration programmatically
     * via setters.
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
     *             Thrown if config file is not present or unreadable
     */
    private void config() throws Exception {

        // Read properties file.
        Properties properties = new Properties();

        if (defaultValues) {
            // FIXME: this should detect if we are running on Jenkins and change
            // these default properties accordingly.

            // Create a directory for testing
            String vmicTemp = "/DFS/ascetic/vm-images/vmic";

            // Set the hostAddress IP for testing currently the private IP for
            // the ip of the host "saas-vm-dev" on the TUB testbed
            properties.setProperty(HOST_ADDRESS_PROPERTY_KEY, "192.168.3.15");

            // Set repositoryPath URI for testing
            properties.setProperty(REPOSITORY_PROPERTY_KEY, vmicTemp);

            // Set rsyncPath URI for testing to local rsync binary
            properties.setProperty(RSYNC_PROPERTY_KEY,
                    "C:\\Users\\django\\cygwin\\bin\\rsync.exe");

            // Set sshPath URI for testing to local ssh binary
            properties.setProperty(SSH_PROPERTY_KEY,
                    "C:\\Users\\django\\cygwin\\bin\\ssh.exe");

            // Set the sskKeyPath URI for testing connectivity to remote SaaS VM
            properties.setProperty(SSH_KEY_PROPERTY_KEY,
                    "C:\\Users\\django\\cygwin\\home\\django\\.ssh\\tub_vm_id_rsa");

            // Set the sshUser name for testing
            properties.setProperty(SSH_USER_PROPERTY_KEY, "ubuntu");

        } else {
            try {
                properties.load(new FileInputStream(
                        this.configPropertiesFileUri));
            } catch (IOException e) {
                LOGGER.error(
                        "The configuration properties file does not exist at location: "
                                + this.configPropertiesFileUri, e);
                throw new Exception(
                        "The configuration properties file does not exist!", e);
            }
        }

        this.hostAddress = properties.getProperty(HOST_ADDRESS_PROPERTY_KEY);
        LOGGER.info("Using hostAddress: '" + hostAddress + "'");

        this.repositoryPath = properties.getProperty(REPOSITORY_PROPERTY_KEY) + "/" + REPOSITORY_PROPERTY;
        LOGGER.info("Using repositoryPath dir: '" + repositoryPath + "'");

        this.rsyncPath = properties.getProperty(RSYNC_PROPERTY_KEY);
        LOGGER.info("Using rsyncPath: '" + rsyncPath + "'");

        this.sshPath = properties.getProperty(SSH_PROPERTY_KEY);
        LOGGER.info("Using sshPath: '" + sshPath + "'");

        this.sshKeyPath = properties.getProperty(SSH_KEY_PROPERTY_KEY);
        LOGGER.info("Using sshKeyPath: '" + sshKeyPath + "'");

        this.sshUser = properties.getProperty(SSH_USER_PROPERTY_KEY);
        LOGGER.info("Using sshUser: '" + sshUser + "'");
    }

    /**
     * @return the defaultValues
     */
    public boolean isDefaultValues() {
        return defaultValues;
    }

    /**
     * Gets the host address that will be used by the VMIC.
     * 
     * @return the hostAddress
     */
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Sets the host address that will be used by the VMIC.
     * 
     * @param hostAddress
     *            the hostAddress to set
     */
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    /**
     * Sets the repository path.
     * 
     * @return the repositoryPath
     */
    public String getRepositoryPath() {
        return repositoryPath;
    }

    /**
     * Gets the repository path.
     * 
     * @param repositoryPath
     *            the repositoryPath to set
     */
    public void setRepositoryPath(String repository) {
        this.repositoryPath = repository;
    }

    /**
     * Gets the path to the rsync binary.
     * 
     * @return the rsyncPath
     */
    public String getRsyncPath() {
        return rsyncPath;
    }

    /**
     * Sets the path to the rsync binary.
     * 
     * @param rsyncPath
     *            the rsyncPath to set
     */
    public void setRsyncPath(String rsyncPath) {
        this.rsyncPath = rsyncPath;
    }

    /**
     * Gets the path to the SSH binary.
     * 
     * @return the sshPath
     */
    public String getSshPath() {
        return sshPath;
    }

    /**
     * Sets the path to the SSH binary.
     * 
     * @param sshPath
     *            the sshPath to set
     */
    public void setSshPath(String sshPath) {
        this.sshPath = sshPath;
    }

    /**
     * Gets the SSH key path.
     * 
     * @return the sshKeyPath
     */
    public String getSshKeyPath() {
        return sshKeyPath;
    }

    /**
     * Sets the SSH key path.
     * 
     * @param sshKeyPath
     *            the sshKeyPath to set
     */
    public void setSshKeyPath(String sshKeyPath) {
        this.sshKeyPath = sshKeyPath;
    }

    /**
     * Gets the SSH user name.
     * 
     * @return the sshUser
     */
    public String getSshUser() {
        return sshUser;
    }

    /**
     * Sets the SSH user name.
     * 
     * @param sshUser
     *            the sshUser to set
     */
    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }
}
