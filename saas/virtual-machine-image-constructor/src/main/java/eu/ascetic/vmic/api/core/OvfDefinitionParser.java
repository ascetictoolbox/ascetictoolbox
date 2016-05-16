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
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.Disk;
import eu.ascetic.utils.ovf.api.File;
import eu.ascetic.utils.ovf.api.Item;
import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.utils.ovf.api.ProductSection;
import eu.ascetic.utils.ovf.api.References;
import eu.ascetic.utils.ovf.api.VirtualSystem;
import eu.ascetic.utils.ovf.api.enums.OperatingSystemType;
import eu.ascetic.utils.ovf.api.enums.ResourceType;
import eu.ascetic.utils.ovf.api.utils.OvfInvalidDocumentException;
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
    private String mode;

    private List<String> imageNames;
    private List<String> imageScripts;
    private List<OperatingSystemType> imageOperatingSystems;
    private List<LinkedHashMap<String, LinkedHashMap<String, String>>> imageSoftwareDependencies;

    private VmicApi vmicApi;

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
        this.ovfDefinitionId = this.ovfDefinition.getVirtualSystemCollection()
                .getId();
        this.vmicApi = vmicApi;
        this.applicationRepositoryPath = this.vmicApi.getGlobalState()
                .getConfiguration().getRepositoryPath() + "/" + ovfDefinitionId;
        LOGGER.info(
                "Application repository path is: " + applicationRepositoryPath);

        // Parse the mode of operation
        try {
            this.mode = this.ovfDefinition.getVirtualSystemCollection()
                    .getProductSectionAtIndex(0).getVmicMode();
            LOGGER.info("VMIC Mode is: " + mode);
        } catch (NullPointerException e) {
            LOGGER.error("The VMIC Mode is not set in OVF!", e);
            this.vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                    .setError(true);
            this.vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                    .setException(e);
            return;
        }

        imageNames = new ArrayList<String>();
        imageScripts = new ArrayList<String>();
        imageOperatingSystems = new ArrayList<OperatingSystemType>();
        imageSoftwareDependencies = new ArrayList<LinkedHashMap<String, LinkedHashMap<String, String>>>();
    }

    /**
     * Parses relevant details from the OVF passed to the class.
     */
    public void parse() {

        // Parse data needed for image generation from the OVF
        VirtualSystem[] virtualSystems = ovfDefinition
                .getVirtualSystemCollection().getVirtualSystemArray();
        LOGGER.info("Parsing " + virtualSystems.length + " Virtual Systems");

        try {
            for (int virtualSystemIndex = 0; virtualSystemIndex < virtualSystems.length; virtualSystemIndex++) {
                VirtualSystem virtualSystem = virtualSystems[virtualSystemIndex];

                Item[] items = virtualSystem.getVirtualHardwareSection()
                        .getItemArray();
                LOGGER.info(
                        "Looking for disk ID in " + items.length + " Items");

                String diskId = findDiskId(items);

                String imageName = findDiskElementFromDiskId(diskId);

                // Parse productSection details for this virtual system
                // depending on VMIC mode
                // FIXME: Assuming only a single product section per Virtual
                // System
                ProductSection productSection = virtualSystem
                        .getProductSectionAtIndex(0);
                if (this.mode.equals("offline")) {
                    parseOfflineProperties(virtualSystemIndex, imageName,
                            productSection);
                } else if (this.mode.equals("online")) {
                    parseOnlineProperties(virtualSystem, imageName,
                            productSection);
                } else {
                    LOGGER.error("Unknown VMIC mode set in OVF! Value is: '"
                            + this.mode
                            + "'. Expected either 'offline' or 'online'");
                    OvfInvalidDocumentException e = new OvfInvalidDocumentException(
                            "Unknown mode set in OVF! Value is: '" + this.mode
                                    + "'. Expected either 'offline' or 'online'",
                            ovfDefinition.getXmlObject());
                    vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                            .setError(true);
                    vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                            .setException(e);
                    return;
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error("Something is not set within OVF!", e);
            vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                    .setError(true);
            vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                    .setException(e);
            return;
        }
    }

    /**
     * Finds the disk ID within the items of a Virtual System.
     * 
     * @param items
     *            The items to search
     * @return the disk ID
     */
    private String findDiskId(Item[] items) {
        String diskId = "";
        for (int j = 0; j < items.length; j++) {
            if (ResourceType.DISK_DRIVE.equals(items[j].getResourceType())) {
                // FIXME: Assuming only one host resource per item (i.e. no
                // support for cow disks)
                LOGGER.debug(items[j].getHostResourceAtIndex(0));

                diskId = items[j]
                        .findHostRosourceId(items[j].getHostResourceAtIndex(0));
                LOGGER.debug("Found diskId: " + diskId);
                break;
            }
        }
        return diskId;
    }

    /**
     * Finds the image name within the disk section for a given disk ID.
     * 
     * @param diskId
     *            The ID to search
     * @return the image name found
     */
    private String findDiskElementFromDiskId(String diskId) {
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
        return imageName;
    }

    /**
     * Parses offline related properties from SaaS modelling component.
     * 
     * @param virtualSystemIndex
     *            The index of the virtual system to get properties for
     * @param imageName
     *            The image name the properties are for
     * @param productSection
     *            The product section to parse
     */
    private void parseOfflineProperties(int virtualSystemIndex,
            String imageName, ProductSection productSection) {

        // Get the vmic script
        String script = productSection.getVmicScript();

        // Add the script replacing variables to local storage
        imageScripts.add(
                replaceVariablesInScript(script, ovfDefinition.getReferences(),
                        getImageMountPointPath(virtualSystemIndex)));

        LOGGER.debug("Added script for image: " + imageName);
    }

    /**
     * Parse online related properties from SaaS modelling tools.
     * 
     * @param virtualSystem
     *            The index of the virtual system to get properties for
     * @param imageName
     *            The image name the properties are for
     * @param productSection
     *            The product section to parse
     */
    private void parseOnlineProperties(VirtualSystem virtualSystem,
            String imageName, ProductSection productSection) {

        // Get the OS for this virtual system (TODO: currently only
        // used in online mode for selecting an image).
        OperatingSystemType os = virtualSystem.getOperatingSystem().getId();
        switch (os) {
        case LINUX:
            // Linux (TODO: add support for distribution flavours
            // and versions)
            imageOperatingSystems.add(OperatingSystemType.LINUX);
            LOGGER.debug("Parsed Linux as OS for image: " + imageName);
            break;
        case MICROSOFT_WINDOWS_SERVER_2003:
            // Windows
            imageOperatingSystems
                    .add(OperatingSystemType.MICROSOFT_WINDOWS_SERVER_2003);
            LOGGER.debug("Parsed Windows 2003 as OS for image: " + imageName);
            break;
        default:
            // Unsupported OS, will cause an error later on
            imageOperatingSystems.add(OperatingSystemType.UNKNOWN);
            LOGGER.warn("Parsed unknown or unsupported OS!");
            break;
        }

        Integer softwareDendencyNumber = productSection
                .getSoftwareDependencyNumber();
        LinkedHashMap<String, LinkedHashMap<String, String>> softwareDependencies = new LinkedHashMap<String, LinkedHashMap<String, String>>();

        LOGGER.debug("Number of software depenencies for image " + imageName
                + " is : " + softwareDendencyNumber);
        for (int j = 0; j < softwareDendencyNumber; j++) {
            Integer packageAttributeNumber = productSection
                    .getSoftwareDependencyPackageAttributeNumber(j);
            // Get URI to package (e.g. chef cookbook zipped up)
            String packageUri = productSection
                    .getSoftwareDependencyPackageUri(j);
            LOGGER.debug("Found package for " + imageName + ", URI is : "
                    + packageUri);
            LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
            // Get attributes describing the package (e.g. chef
            // cookbook attributes)
            for (int k = 0; k < packageAttributeNumber; k++) {
                String key = productSection
                        .getSoftwareDependencyPackageAttributeName(j, k);
                String value = productSection
                        .getSoftwareDependencyPackageAttributeValue(j, k);
                LOGGER.debug("Found attribute for package, key:value is: " + key
                        + " = " + value);
                attributes.put(key, value);
            }
            softwareDependencies.put(packageUri, attributes);
        }
        imageSoftwareDependencies.add(softwareDependencies);
    }

    /**
     * Method to replace variables within an PM Plugin script passed to the VMIC
     * via OVF.
     * 
     * @param scriptIn
     *            The script to replace variables in
     * @param references
     *            The references to use in the variable replacement
     * @param mountPoint
     *            The mount point of the image that the script will operate
     *            within
     * @return The script with variables replaced.
     */
    public static String replaceVariablesInScript(String scriptIn,
            References references, String mountPoint) {

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
                "var/lib/tomcat7/webapps");

        // Set the mount point
        scriptOut = scriptOut.replace("${MOUNT_POINT}", mountPoint);

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
     * @return The mode: Offline or Online
     */
    public String getMode() {
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
        return imageScripts.get(i);
    }

    /**
     * Gets the base image path selected for use an image as defined by the
     * requirements specified in the OVF
     * 
     * @return The base image path
     */
    public String getBaseImagePath(int i) {
        // TODO: FIXME: Using hard coded image URI, for Y2 select using OVF
        // operating
        // system section
        LOGGER.warn(
                "Using hardcoded base image path: /mnt/cephfs/ascetic/vmic/base-images/linux/deb-wheezy.raw.img");
        return "/mnt/cephfs/ascetic/vmic/base-images/linux/deb-wheezy.raw.img";
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

    /**
     * Gets the Operating System of an image.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The {@link OperatingSystemType} of the image
     */
    public OperatingSystemType getImageOperatingSystems(int i) {
        return imageOperatingSystems.get(i);
    }

    /**
     * Gets the number of software dependencies associated with an image.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return The number of software dependencies of the image
     */
    public int getImageSoftwareDependencyNumber(int i) {
        return imageSoftwareDependencies.get(i).size();
    }

    /**
     * Gets the nested Map describing an image's software dependencies.
     * 
     * @param i
     *            The index i of the image defined by the order in which the
     *            disks appears in the OVF
     * @return A LinkedHashMap describing the software dependencies of type:
     *         LinkedHashMap&ltString <i>packageUri</i>, LinkedHashMap
     *         <i>attributes</i>&gt, where <i>attributes</i> is a nested
     *         LinkedHashMap of type: LinkedHashMap&ltString <i>attributeKey</i>
     *         , String <i>attributeValue</i>&gt
     */
    public LinkedHashMap<String, LinkedHashMap<String, String>> getImageSoftwareDependencies(
            int i) {
        return imageSoftwareDependencies.get(i);
    }
}
