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
package eu.ascetic.vmic.api.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import eu.ascetic.vmic.api.VmicApi;

/**
 * Provides access to data in an OVF document required to generate and parse an
 * image.
 * 
 * @author Django Armstrong (ULeeds)
 *
 */
public class OvfDefinitionParser {
    protected final static Logger LOGGER = Logger
            .getLogger(OvfDefinitionParser.class);

    private OvfDefinition ovfDefinition;
    private String ovfDefinitionId;
    private String applicationRepositoryPath;
    private int mode;

    private List<String> imageNames;
    private List<String> imageScripts;

    /**
     * Constructor for setting up the parser.
     * 
     * @param ovfDefinition
     *            The OVF to parse
     * @param vmicApi
     *            The VMIC API instance for accessing global state and
     *            configuration
     */
    public OvfDefinitionParser(OvfDefinition ovfDefinition, VmicApi vmicApi) {
        this.ovfDefinition = ovfDefinition;
        this.ovfDefinitionId = ovfDefinition.getVirtualSystemCollection()
                .getId();
        this.applicationRepositoryPath = vmicApi.getGlobalState()
                .getConfiguration().getRepositoryPath()
                + "/" + ovfDefinitionId;
        LOGGER.info("Application repository path is: "
                + applicationRepositoryPath);

        // Parse the mode of operation
        this.mode = Integer.parseInt(ovfDefinition.getVirtualSystemCollection()
                .getProductSectionAtIndex(0)
                .getPropertyByKey("asceticVmicMode").getValue());
        LOGGER.info("VMIC Mode is: " + mode);

        imageNames = new ArrayList<String>();
        imageScripts = new ArrayList<String>();
    }

    /**
     * Parses relevant details from the OVF passed to the class.
     */
    public void parse() {

        // Parse data needed for image generation from the OVF
        VirtualSystem[] virtualSystems = ovfDefinition
                .getVirtualSystemCollection().getVirtualSystemArray();
        LOGGER.info("Parsing " + virtualSystems.length + " Virtual Systems");

        for (int i = 0; i < virtualSystems.length; i++) {
            VirtualSystem virtualSystem = virtualSystems[i];

            Item[] items = virtualSystem.getVirtualHardwareSection()
                    .getItemArray();
            LOGGER.info("Looking for disk ID in " + items.length + " of Items");

            // Find the disk ID for this Virtual System
            String diskId = "";
            for (int j = 0; j < items.length; j++) {
                if (ResourceType.DISK_DRIVE.equals(items[j].getResourceType())) {
                    // FIXME: Assuming only one host resource per item (i.e. no
                    // support for cow disks)
                    LOGGER.debug(items[j].getHostResourceAtIndex(0));

                    diskId = items[j].findHostRosourceId(items[j]
                            .getHostResourceAtIndex(0));
                    LOGGER.debug("Found diskId: " + diskId);
                }
            }

            // Find the disk element from the disk ID.
            String imageName = null;
            Disk[] disks = ovfDefinition.getDiskSection().getDiskArray();
            for (int j = 0; j < disks.length; j++) {
                if (disks[j].getDiskId().equals(diskId)) {
                    String refId = disks[j].getFileRef();
                    imageName = refId + ".img";
                    // Set the image name
                    imageNames.add(imageName);
                    LOGGER.debug("Added image name: " + imageName);
                    // Add the new file references
                    String href = applicationRepositoryPath + "/" + imageName;
                    File file = File.Factory.newInstance(refId, href);
                    ovfDefinition.getReferences().addFile(file);
                    break;
                }
            }

            // Get the script for this virtual system
            // FIXME: Assuming only a single product section per Virtual System
            ProductSection productSection = virtualSystem
                    .getProductSectionAtIndex(0);
            String script = productSection
                    .getPropertyByKey("asceticVmicScript").getValue();

            // Add the script replacing variables to local storage
            imageScripts.add(replaceVariablesInScript(script,
                    ovfDefinition.getReferences()));
            LOGGER.debug("Added script for image: " + imageName);
        }
    }

    /**
     * @param scriptIn
     * @param references
     */
    public static String replaceVariablesInScript(String scriptIn,
            References references) {

        LOGGER.debug("Script before variable replacement is: ");
        LOGGER.debug(scriptIn);

        String scriptOut = scriptIn;

        File[] files = references.getFileArray();
        for (int i = 0; i < files.length; i++) {
            scriptOut = scriptOut.replace("${" + files[i].getId() + "}",
                    files[i].getHref());
        }

        // FIXME: This should be fetched mapped to a specific OS defined in the
        // OVF
        scriptOut = scriptOut.replace("${IMAGE_WEBAPP_FOLDER}",
                "/var/lib/tomcat7/webapps");

        LOGGER.debug("Script after variable replacement is: ");
        LOGGER.debug(scriptOut);
        return scriptOut;
    }

    /**
     * Gets the path to the application's file repository.
     * 
     * @return the applicationRepositoryPath The path
     */
    public String getApplicationRepositoryPath() {
        return applicationRepositoryPath;
    }

    /**
     * Gets the mode of operation that the VMIC will run in.
     * 
     * @return The mode: Offline = 1, Online = 2
     */
    public int getMode() {
        return mode;
    }

    /**
     * Gets the number of disk images parsed from the OVF.
     * 
     * @return The number of images
     */
    public int getImageNumber() {
        return imageNames.size();
    }

    /**
     * Gets the path to an image.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The image path
     */
    public String getImagePath(int i) {
        return applicationRepositoryPath + "/" + getImageName(i);
    }

    /**
     * Gets an image's path to its mount point.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The mount point
     */
    public String getImageMountPointPath(int i) {
        return applicationRepositoryPath + "/mnt/" + getImageName(i);
    }

    /**
     * Gets the script for an image.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The script for the image
     */
    public String getScript(int i) {
        return null;
    }

    /**
     * Gets the base image path selected for use an image as defined by the
     * requirements specified in the OVF
     * 
     * @return The base image path
     */
    public String getBaseImagePath(int i) {
        // FIXME: Using hard coded image URI, for Y2 select using OVF operating
        // system section
        LOGGER.warn("Using hardcoded base image path: /DFS/ascetic/vm-images/Ubuntu.qcow2");
        return "/DFS/ascetic/vm-images/Ubuntu.qcow2";
    }

    /**
     * Gets the name of an image.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The name of the image file
     */
    private String getImageName(int i) {
        return imageNames.get(i);
    }
}
