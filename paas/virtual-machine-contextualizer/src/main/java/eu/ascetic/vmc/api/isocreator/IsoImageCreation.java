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
package eu.ascetic.vmc.api.isocreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmc.api.core.SystemCall;
import eu.ascetic.vmc.api.core.SystemCallException;
import eu.ascetic.vmc.api.datamodel.ContextData;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmc.api.datamodel.VirtualMachine;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.EndPoint;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.SecurityKey;
import eu.ascetic.vmc.api.datamodel.contextdatatypes.SoftwareDependency;
import eu.ascetic.vmc.api.datamodel.image.Iso;

/**
 * Class to create ISO images for containing contextualization data.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.4
 */
public class IsoImageCreation {

    private static final String IO_EXCEPTION = "IOException: ";

    private static final String FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException: ";

    private static final String CREATED_FILE = "Created file: ";

    private static final String ATTEMPTING_TO_CREATE_FILE = "Attempting to create file: ";

    protected static final Logger LOGGER = Logger
            .getLogger(IsoImageCreation.class);

    private Iso iso;
    private GlobalConfiguration configuration;
    private SystemCall systemCall;
    private String isoDataDirectory;
    private Boolean addRecontextFiles;

    private OvfDefinition ovfDefinition;

    private ContextData contextData;
    private VirtualMachine virtualMachine;

    /**
     * Initialises an instance of the ISO Image creator.
     * 
     * @param iso
     *            The ISO to create.
     * @param configuration
     *            Configuration details used when creating the ISO.
     */
    public IsoImageCreation(Iso iso, GlobalConfiguration configuration,
            OvfDefinition ovfDefinition) {
        this.iso = iso;
        this.configuration = configuration;
        this.ovfDefinition = ovfDefinition;
        this.systemCall = new SystemCall(configuration.getInstallDirectory());
        this.isoDataDirectory = configuration.getContextDataDirectory()
                + File.separator + iso.getFileName();
        this.addRecontextFiles = configuration.getAddRecontextFiles();
        LOGGER.debug("Read addRecontextFiles from config: "
                + this.addRecontextFiles);

        // Create the iso output directory
        if (!new File(iso.getUri().substring(0,
                iso.getUri().lastIndexOf(File.separator))).mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for iso output directory");
        }
    }

    /**
     * Create new IsoImageCreation object for recontextualization
     * 
     * @param configuration
     *            The global configuration
     * @param existingIsoPath
     *            The path to the existing ISO file
     * @throws IOException
     *             If the existing ISO cannot be found, or if a temp directory
     *             cannot be created.
     */
    public IsoImageCreation(GlobalConfiguration configuration,
            String existingIsoPath) throws IOException {
        String fileName = "recontext_" + existingIsoPath.substring(
                existingIsoPath.lastIndexOf(File.separator) + 1,
                existingIsoPath.length());
        // Not used in this context, for VirtualMachine
        String imageId = "0";
        String uri = configuration.getRepository() + File.separator + fileName;
        String format = "ISO9660";
        // TODO: Not currently used? Investigate.
        String mountPoint = "/media/context/";
        this.iso = new Iso(imageId, fileName, uri, format, mountPoint);
        this.configuration = configuration;
        this.systemCall = new SystemCall(configuration.getInstallDirectory());
        this.isoDataDirectory = configuration.getContextDataDirectory()
                + File.separator + iso.getFileName();
        this.addRecontextFiles = configuration.getAddRecontextFiles();

        // Create the iso output directory
        if (!new File(iso.getUri().substring(0,
                iso.getUri().lastIndexOf(File.separator))).mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for iso output directory");
        }
    }

    /**
     * 0) Store the INSTANCE_ID and SERVICE_ID files
     */
    private void storeIds() {

        String instanceId = virtualMachine.getComponentId() + "_"
                + iso.getImageId();
        File instanceIdFile = new File(
                isoDataDirectory + File.separator + "INSTANCE_ID");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(instanceIdFile);
            fos.write(instanceId.getBytes(StandardCharsets.ISO_8859_1));
            LOGGER.debug(
                    "Writing instance ID: \"" + instanceId + "\" complete!");

        } catch (FileNotFoundException e) {
            LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
        } catch (IOException e) {
            LOGGER.error(IO_EXCEPTION + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error(IO_EXCEPTION + e);
            }
        }

        String serviceId = ovfDefinition.getVirtualSystemCollection()
                .getProductSectionAtIndex(0).getDeploymentId();
        File serviceIdFile = new File(
                isoDataDirectory + File.separator + "SERVICE_ID");
        fos = null;
        try {
            fos = new FileOutputStream(serviceIdFile);
            fos.write(serviceId.getBytes(StandardCharsets.ISO_8859_1));
            LOGGER.debug("Writing service ID: \"" + serviceId + "\" complete!");
        } catch (FileNotFoundException e) {
            LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
        } catch (IOException e) {
            LOGGER.error(IO_EXCEPTION + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error(IO_EXCEPTION + e);
            }
        }

    }

    /**
     * 1) Store the security keys if the are to be added to this VM instance
     */
    private void storeSecurityKeys() { // NOSONAR Complexity necessary
        File securityKeysDirectory = new File(
                isoDataDirectory + File.separator + "securitykeys");

        if (!securityKeysDirectory.mkdirs()) {
            LOGGER.warn(
                    "Failed to invoke mkdirs() for security keys directory");
        }

        if (!contextData.getSecurityKeys().isEmpty()) {
            for (SecurityKey securityKey : contextData.getSecurityKeys()
                    .values()) {
                String name = securityKey.getName();
                byte[] keyData = securityKey.getKeyData();

                File securityKeyFile = null;
                // TODO: change to switch... meh
                if (name.equals("ssh-public")) {
                    securityKeyFile = new File(securityKeysDirectory
                            + File.separator + "ssh-public.key");
                } else if (name.equals("ssh-private")) {
                    securityKeyFile = new File(securityKeysDirectory
                            + File.separator + "ssh-private.key");
                } else {
                    LOGGER.error("Unknown key name type!");
                    return;
                }

                try {
                    LOGGER.debug(ATTEMPTING_TO_CREATE_FILE
                            + securityKeyFile.getPath());
                    if (!securityKeyFile.createNewFile()) {
                        LOGGER.warn("Security key file already exists!");
                    }
                    LOGGER.debug(CREATED_FILE + securityKeyFile.getPath());
                } catch (IOException e) {
                    LOGGER.error(
                            "Failed to create security key file with name: "
                                    + name + ".key",
                            e);
                }

                // Write out security key...
                LOGGER.warn("Writing out security key with path: "
                        + securityKeyFile.getPath());
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(securityKeyFile);
                    fos.write(keyData);
                    LOGGER.debug("Writing security key complete!");

                } catch (FileNotFoundException e) {
                    LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
                } catch (IOException e) {
                    LOGGER.error(IO_EXCEPTION + e);
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to close fileOutputStream");
                    }
                }
            }
        } else {
            LOGGER.warn("No security keys to write!");
        }
    }

    /**
     * 2) Write out the end points to each to there own file in a sub-directory:
     */
    private void storeEndpoints() {

        File endPointDirectory = new File(
                isoDataDirectory + File.separator + "endpoints");
        if (!endPointDirectory.mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for end point directory");
        }

        if (virtualMachine.getEndPoints().size() != 0) {
            for (EndPoint endPoint : virtualMachine.getEndPoints().values()) {
                String id = endPoint.getId();
                String uri = endPoint.getUri();
                String type = endPoint.getType();
                String subtype = endPoint.getSubtype();
                String interval = endPoint.getInterval();

                // Create the end point with the given name
                File endPointFile = new File(endPointDirectory + File.separator
                        + id + ".properties");
                try {
                    LOGGER.debug(
                            ATTEMPTING_TO_CREATE_FILE + endPointFile.getPath());
                    if (!endPointFile.createNewFile()) {
                        LOGGER.warn("End point file already exists!");
                    }
                    LOGGER.debug(CREATED_FILE + endPointFile.getPath());
                } catch (IOException e) {
                    LOGGER.error("Failed to create endpoint file with name: "
                            + id + ".properties", e);
                }

                Properties props = new Properties();
                props.setProperty("id", id);
                props.setProperty("uri", uri);
                props.setProperty("type", type);
                props.setProperty("subtype", subtype);
                props.setProperty("interval", interval);

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(endPointFile);
                    props.store(fileOutputStream,
                            "VMC properties file for end points:");
                    LOGGER.debug("Writing endpoint complete!");

                } catch (FileNotFoundException e) {
                    LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
                } catch (IOException e) {
                    LOGGER.error(IO_EXCEPTION + e);
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to close fileOutputStream");
                    }
                }

            }
        } else {
            LOGGER.warn("No end points to write!");
        }
    }

    /**
     * 3) Provides per VM instance configuration information for software
     * dependencies
     */
    private void storeSoftwareDependencies() { // NOSOAR Complexity necessary

        // TODO: Create a script to invoke the software dependency installation
        // scripts and store it in \scripts\softwaredeps.sh

        File softwareDependenciesDirectory = new File(
                isoDataDirectory + File.separator + "softwaredeps");
        if (!softwareDependenciesDirectory.mkdirs()) {
            LOGGER.warn(
                    "Failed to invoke mkdirs() for software dependencies directory");
        }

        if (!virtualMachine.getSoftwareDependencies().isEmpty()) {
            for (SoftwareDependency softwareDependency : virtualMachine
                    .getSoftwareDependencies().values()) {
                String id = softwareDependency.getId();
                String type = softwareDependency.getType();
                String packageUri = softwareDependency.getPackageUri();
                String installScriptUri = softwareDependency
                        .getInstallScriptUri();

                // Create the software dependency files here...
                File softwareDependencyFile = new File(
                        softwareDependenciesDirectory + File.separator + id
                                + ".properties");

                try {
                    LOGGER.debug(ATTEMPTING_TO_CREATE_FILE
                            + softwareDependencyFile.getPath());
                    if (!softwareDependencyFile.createNewFile()) {
                        LOGGER.warn("Software dependency file already exists!");
                    }
                    LOGGER.debug(
                            CREATED_FILE + softwareDependencyFile.getPath());
                } catch (IOException e) {
                    LOGGER.error(
                            "Failed to create software dependency property file with name: "
                                    + id + ".properties",
                            e);
                }

                Properties props = new Properties();
                props.setProperty("id", id);
                props.setProperty("type", type);

                // Write out properties
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(
                            softwareDependencyFile);
                    props.store(fileOutputStream,
                            "VMC properties file for software dependencies:");
                    LOGGER.debug(
                            "Writing software dependencie property file complete!");
                } catch (FileNotFoundException e) {
                    LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
                } catch (IOException e) {
                    LOGGER.error(IO_EXCEPTION + e);
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to close fileOutputStream");
                    }
                }

                // Create the directory to store the software dependency files
                File softwareDependencyDirectory = new File(
                        softwareDependenciesDirectory + File.separator + id);
                if (!softwareDependencyDirectory.mkdirs()) {
                    LOGGER.warn(
                            "Failed to invoke mkdirs() for software dependency directory");
                }
                LOGGER.debug("Creating directory for software dependency: "
                        + softwareDependencyDirectory.getPath());

                // Store the software dependency package from the URI
                fileOutputStream = null;
                try {
                    LOGGER.debug(
                            "Copying software dependency package from uri: "
                                    + packageUri);

                    Path packageUriPath = Paths.get(packageUri);

                    File softwareDependencyPackage = new File(
                            softwareDependencyDirectory + File.separator
                                    + packageUriPath.getFileName());

                    fileOutputStream = new FileOutputStream(
                            softwareDependencyPackage);

                    Files.copy(packageUriPath, fileOutputStream);

                    LOGGER.debug("Copied software dependency package to: "
                            + softwareDependencyPackage.getPath());
                } catch (IOException e) {
                    LOGGER.error(
                            "Failed to create software dependency with id: "
                                    + id,
                            e);
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to close fileOutputStream");
                    }
                }

                // Store the script that installs and configures the software
                // dependency from the URI
                fileOutputStream = null;
                try {
                    LOGGER.debug(
                            "Copying software dependency install script from uri: "
                                    + installScriptUri);

                    Path installScriptUriPath = Paths.get(installScriptUri);

                    File softwareDependencyPackage = new File(
                            softwareDependencyDirectory + File.separator
                                    + installScriptUriPath.getFileName());

                    fileOutputStream = new FileOutputStream(
                            softwareDependencyPackage);

                    Files.copy(installScriptUriPath, fileOutputStream);

                    LOGGER.debug(
                            "Copied software dependency install script to: "
                                    + softwareDependencyPackage.getPath());
                } catch (IOException e) {
                    LOGGER.error(
                            "Failed to create software dependency with id: "
                                    + id,
                            e);
                } finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to close fileOutputStream");
                    }
                }

            }
        } else {
            LOGGER.warn("No software dependency configuration files to write!");
        }
    }

    /**
     * 4) Store the entire ovfDefinition
     */
    private void storeOvfDefinition() {
        File ovfDefinitionFile = new File(
                isoDataDirectory + File.separator + "ovf.xml");
        FileOutputStream fos = null;
        try {
            LOGGER.debug(
                    ATTEMPTING_TO_CREATE_FILE + ovfDefinitionFile.getPath());
            if (!ovfDefinitionFile.createNewFile()) {
                LOGGER.warn("OVF definition file already exists!");
            }
            LOGGER.debug(CREATED_FILE + ovfDefinitionFile.getPath());

            // Write out the OVF Definition file
            fos = new FileOutputStream(ovfDefinitionFile.getPath());
            fos.write(ovfDefinition.toString()
                    .getBytes(StandardCharsets.ISO_8859_1));
            LOGGER.debug("Writing OVF Definition complete!");
        } catch (IOException e) {
            LOGGER.error("Failed to create OVF Definition file with name: "
                    + ovfDefinitionFile.getName(), e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close fileOutputStream");
            }
        }
    }

    /**
     * 5) Bootstrap scripts
     * 
     * @param scriptsDirectory
     *            The directory to copy scripts from
     */
    private void storeBootstrapScripts(File scriptsDirectory) {
        File bootStrapFile = new File(
                scriptsDirectory + File.separator + "bootstrap.sh");

        FileOutputStream fos = null;
        try {
            LOGGER.debug(ATTEMPTING_TO_CREATE_FILE + bootStrapFile.getPath());
            if (!bootStrapFile.createNewFile()) {
                LOGGER.warn("Boostrap file already exists!");
            }
            LOGGER.debug(CREATED_FILE + bootStrapFile.getPath());

            // TODO: This should be stored somewhere else and not hard coded
            // Mount location is currently hard coded in the init.d scripts of
            // the base VM /mnt/context/
            String bootStrapScript = "#!/bin/bash\n"
                    + "if [ -f /mnt/context/scripts/bootstrap.sh ]; then\n"
                    + "  mkdir -p /root/.ssh/\n"
                    + "  #Get the public SSH key:\n"
                    + "  cat /mnt/context/securitykeys/ssh-public.key >> /root/.ssh/authorized_keys\n"
                    + "  chmod 700 /root/.ssh\n"
                    + "  chmod 600 /root/.ssh/authorized_keys\n"
                    + "  cat /mnt/context/securitykeys/ssh-public.key > /root/.ssh/id_rsa.pub\n"
                    + "  chmod 600 /root/.ssh/id_rsa.pub\n"
                    + "  #Get the private SSH key:\n"
                    + "  cat /mnt/context/securitykeys/ssh-private.key > /root/.ssh/id_rsa\n"
                    + "  chmod 700 /root/.ssh/id_rsa\n"
                    + "  #Run software dependency install script if present\n"
                    + "  if [ -f /mnt/context/scripts/softwaredeps.sh ]; then\n"
                    + "    sh /mnt/context/scripts/softwaredeps.sh\n" + "  fi\n"
                    + "fi\n";

            // Write out the boostrap file
            fos = new FileOutputStream(bootStrapFile.getPath());
            fos.write(bootStrapScript.getBytes(StandardCharsets.ISO_8859_1));
            LOGGER.debug("Writing boobstrap script complete!");
        } catch (IOException e) {
            LOGGER.error("Failed to create boobstrap script file with name: "
                    + bootStrapFile.getName(), e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close fileOutputStream");
            }
        }
    }

    /**
     * 6) Add recontextualization files
     * 
     * @param addRecontextScripts
     *            True if we are to add recontextualization scripts
     */
    private void refactorFiles(boolean addRecontextScripts) { // NOSONAR
        // FIXME: Change this to an ISO structure version number
        if (addRecontextFiles) {
            LOGGER.debug("Performing ISO recontextualization changes...");

            LOGGER.debug("Restructuring ISO");
            // Create the data directory
            String recontextIsoDataDirectory = isoDataDirectory + File.separator
                    + "data";
            // Move the contents in the root of the iso to the new data
            // directory
            LOGGER.debug("Copying context data from: '" + isoDataDirectory
                    + "' to: '" + recontextIsoDataDirectory + "'");

            File recontextIsoDataDirectoryFile = new File(
                    recontextIsoDataDirectory);
            if (!recontextIsoDataDirectoryFile.mkdir()) {
                LOGGER.warn(
                        "Failed to invoke mkdirs() for recontext iso data directory");
            }

            File[] fileList = new File(isoDataDirectory).listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    try {
                        if (file.isDirectory()) {
                            LOGGER.debug(
                                    "Considering directory: " + file.getName());
                            if (!file.getName().equalsIgnoreCase("data")) {
                                File newDir = new File(
                                        recontextIsoDataDirectoryFile
                                                + File.separator
                                                + file.getName());
                                FileUtils.copyDirectory(file, newDir);
                                LOGGER.debug("Copied directory: "
                                        + file.getAbsolutePath() + " to "
                                        + newDir.getAbsolutePath());
                            }
                        } else {
                            FileUtils.copyFile(file,
                                    new File(recontextIsoDataDirectoryFile
                                            + File.separator + file.getName()));
                            LOGGER.debug("Copying file: " + file.getName());
                        }

                    } catch (IOException e) {
                        LOGGER.error(
                                "Failed to copy old context data to recontext data directory",
                                e);
                    }
                }
            }

            // Delete old context data
            LOGGER.debug("Deleting old context data");
            fileList = new File(isoDataDirectory).listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (!file.getName().equalsIgnoreCase("data")) {
                        try {
                            FileUtils.forceDelete(file);
                            LOGGER.debug("Deleted file or directory: '"
                                    + file.getPath() + "'");
                        } catch (IOException e) {
                            LOGGER.error("Failed to delete file or directory: '"
                                    + file.getPath() + "'", e);
                        }
                    } else {
                        LOGGER.debug("Skipping deletion of 'data' directory");
                    }
                }
            }

            // Add the recontext-scripts directory to the iso
            if (addRecontextScripts) {
                String recontextIsoScriptDirectory = isoDataDirectory
                        + File.separator + "recontext-scripts";
                String recontextScripts = configuration.getInstallDirectory()
                        + File.separator + "templates" + File.separator
                        + "scripts" + File.separator + "recontext";
                LOGGER.debug("Copying recontext files from: '"
                        + recontextScripts + "' to: '"
                        + recontextIsoScriptDirectory + "'");
                try {
                    FileUtils.copyDirectory(new File(recontextScripts),
                            new File(recontextIsoScriptDirectory));
                } catch (IOException e) {
                    LOGGER.error(
                            "Failed to copy old context data to recontext data directory",
                            e);
                }
            }

            LOGGER.debug("ISO recontextualization changes completed");

        } else {
            LOGGER.debug(
                    "Recontext files have not been specified in the VMC config, not altering ISO structure!");
        }
    }

    /**
     * 7 ) Add ISO version info to props file
     * 
     * @param version
     *            The version of the context data
     * @param type
     *            The ISO type (context vs recontext)
     */
    private void storeMetaDataFile(String version, String type) {
        LOGGER.debug(
                "Creating meta.data properties file for folder stucture version="
                        + version);
        Properties metaDataVersion = new Properties();
        metaDataVersion.setProperty("version", version);
        metaDataVersion.setProperty("type", type);
        File metaDataFile = new File(
                isoDataDirectory + File.separator + ".metadata");

        LOGGER.debug("Adding meta.data to ISO");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(metaDataFile);
            metaDataVersion.store(fileOutputStream,
                    "VMC ISO data structure version:");
            LOGGER.debug("Writing endpoint complete!");
        } catch (FileNotFoundException e) {
            LOGGER.error(FILE_NOT_FOUND_EXCEPTION + e);
        } catch (IOException e) {
            LOGGER.error(IO_EXCEPTION + e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close fileOutputStream");
            }
        }
    }

    /**
     * Store context data to the local repository. The following context data is
     * stored: 1) Security Keys 2) End Points 3) Software Dependencies 4)
     * License Tokens 5) Entire OVF Definition 6) Bootstrap script 7) Security
     * Agents
     * 
     * TODO: Refactor reducing duplication of code used for directory and file
     * operations...
     * 
     * @param contextData
     *            Used to access the security keys applied at the service level.
     * @param virtualMachine
     *            The virtual machine to store context data for.
     */
    public void storeContextData(ContextData contextData,
            VirtualMachine virtualMachine) {

        this.contextData = contextData;
        this.virtualMachine = virtualMachine;

        LOGGER.debug(
                "Iso Data Directory is: " + isoDataDirectory + File.separator);

        // Create the directory
        if (!new File(isoDataDirectory).mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for iso data directory");
        }

        storeIds();

        storeSecurityKeys();

        storeEndpoints();

        storeSoftwareDependencies();

        storeOvfDefinition();

        // Create scripts directory for bootstrap and agents
        File scriptsDirectory = new File(
                isoDataDirectory + File.separator + "scripts");
        if (!scriptsDirectory.mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for scripts directory");
        }

        storeBootstrapScripts(scriptsDirectory);

        // Refactor the ISO for recontext support
        refactorFiles(true);

        storeMetaDataFile("2", "context");
    }

    /**
     * Creates a new recontextualization image (with test data), eventually will
     * take as input recontextualization data specific to an IP
     * 
     * @param contextdata
     *            The context data to use for recontextualization
     * @param virtualMachine
     *            The virtual machine the recontext data is for
     */
    public void storeRecontextData(ContextData contextdata,
            VirtualMachine virtualMachine) {

        // TODO: contextData used for security keys but not implemented in this
        // function yet
        this.contextData = contextdata;
        this.virtualMachine = virtualMachine;

        LOGGER.debug(
                "Iso Data Directory is: " + isoDataDirectory + File.separator);

        // Create the directory
        if (!new File(isoDataDirectory).mkdirs()) {
            LOGGER.warn("Failed to invoke mkdirs() for iso data directory");
        }

        // Add endpoints to be stored in the recontext ISO
        storeEndpoints();

        // Refactor the ISO for recontext support without adding the recontext
        // scripts
        refactorFiles(false);

        storeMetaDataFile("2", "recontext");
    }

    /**
     * Create an ISO using its associated attributes and stored context data.
     * 
     * @return The ISO created.
     * @throws SystemCallException
     *             Thrown if the command to create the ISO via a system call
     *             fails.
     */
    public Iso create() throws SystemCallException { // NOSONAR Complexity necessary

        // Detect Linux distribution
        String commandName;
        if (new File("/etc/debian_version").exists()) {
            LOGGER.info(
                    "Debian distribution Variant detected using \"genisoimage\"");
            commandName = "genisoimage";
        } else if (new File("/etc/redhat-release").exists()) {
            LOGGER.info(
                    "Redhat distribution variant detected using \"mkisofs\"");
            commandName = "mkisofs";
        } else {
            LOGGER.info(
                    "Unknown linux distribution detected using default \"mkisofs\"");
            commandName = "mkisofs";
        }

        ArrayList<String> arguments = new ArrayList<String>();

        // Generate SUSP and RR records
        arguments.add("-R");
        // File ownership and modes
        arguments.add("-r");
        // Generate Joliet directory records
        arguments.add("-J");
        // Allow full 31 character filenames
        arguments.add("-l");
        arguments.add("-allow-leading-dots");
        arguments.add("-allow-lowercase");
        arguments.add("-allow-multidot");
        // filename
        arguments.add("-o");
        arguments.add(iso.getUri());
        arguments.add(isoDataDirectory);

        // Executed command looks like so:
        // "mkisofs -o iso.getFileName() isoDataDirectory"
        try {
            systemCall.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            if (configuration.isDefaultValues()) {
                LOGGER.warn(
                        "Failed to run command, is this invocation in a unit test?",
                        e);
            } else {
                throw e;
            }
        }

        if (systemCall.getReturnValue() == 0) {
            iso.setCreated(true);
            LOGGER.info("Iso created with uri: " + iso.getUri());
        } else {
            LOGGER.error("Iso Creation Failed! Return value was: "
                    + systemCall.getReturnValue());
        }

        // Print out the directory tree structure for debug purposes:
        LOGGER.debug(
                "Files in directory: " + isoDataDirectory + File.separator);
        try {
            List<File> files = getFileListing(new File(isoDataDirectory));
            for (File file : files) {
                LOGGER.debug(file);
            }
        } catch (Exception e) {
            LOGGER.error("File was not found while listing directory!", e);
        }

        // Remove isoDataDirectory recursively after creating the ISO:
        try {
            deleteRecursive(new File(isoDataDirectory));
            LOGGER.debug("Recursively deleted isoDataDirectory: "
                    + isoDataDirectory + File.separator);
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot recursively delete isoDataDirectory: "
                    + isoDataDirectory, e);
        } catch (NotDirectoryException e) {
            LOGGER.error("Cannot recursively delete isoDataDirectory: "
                    + isoDataDirectory, e);
        }

        return iso;
    }

    /**
     * Delete a directory recursively, this does the equivalent of "rm -r".
     * 
     * @param path
     *            Root File Path.
     * @return True if the file and all sub files/directories have been removed.
     *         False if not a directory or a file was not removed.
     * @throws NotDirectoryException 
     * @throws FileNotFoundException.
     */
    private static boolean deleteRecursive(File path)
            throws FileNotFoundException, NotDirectoryException {
        if (!path.exists()) {
            throw new FileNotFoundException(path.getAbsolutePath());
        }

        boolean ret = true;
        File[] fileList = path.listFiles();
        if (fileList != null) {
            for (File f : fileList) {
                ret = ret && deleteRecursive(f);
            }
            return ret && path.delete();
        } else {
            throw new NotDirectoryException(path.getAbsolutePath());
        }
    }

    /**
     * Recursively walk a directory tree and return a List of all Files found;
     * the List is sorted using File.compareTo().
     * 
     * TODO: move this to a utils class..
     * 
     * @param aStartingDir
     *            is a valid directory, which can be read.
     * @throws NotDirectoryException 
     */
    private static List<File> getFileListing(File aStartingDir)
            throws FileNotFoundException, NotDirectoryException {
        validateDirectory(aStartingDir);
        List<File> result = getFileListingNoSort(aStartingDir);
        Collections.sort(result);
        return result;
    }

    /**
     * Get file list without sorting.
     * 
     * TODO: Move this to a utils class...
     * 
     * @param aStartingDir
     *            The starting directory.
     * @return List of files found.
     * @throws FileNotFoundException
     * @throws NotDirectoryException 
     */
    private static List<File> getFileListingNoSort(File aStartingDir)
            throws FileNotFoundException, NotDirectoryException {
        List<File> result = new ArrayList<File>();
        File[] filesAndDirs = aStartingDir.listFiles();
        if (filesAndDirs == null) {
            throw new NotDirectoryException(aStartingDir.getAbsolutePath());
        }
        List<File> filesDirs = Arrays.asList(filesAndDirs);
        for (File file : filesDirs) {
            // always add, even if directory
            result.add(file);
            if (!file.isFile()) {
                // must be a directory
                // recursive call!
                List<File> deeperList = getFileListingNoSort(file);
                result.addAll(deeperList);
            }
        }
        return result;
    }

    /**
     * Directory is valid if it exists, does not represent a file, and can be
     * read.
     * 
     * TODO: Move this to a utils class...
     */
    private static void validateDirectory(File aDirectory)
            throws FileNotFoundException {
        if (aDirectory == null) {
            throw new IllegalArgumentException("Directory should not be null.");
        }
        if (!aDirectory.exists()) {
            throw new FileNotFoundException(
                    "Directory does not exist: " + aDirectory);
        }
        if (!aDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Is not a directory: " + aDirectory);
        }
        if (!aDirectory.canRead()) {
            throw new IllegalArgumentException(
                    "Directory cannot be read: " + aDirectory);
        }
    }

}
