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
import java.util.Vector;

import org.apache.log4j.Logger;

import eu.ascetic.utils.ovf.api.OvfDefinition;
import eu.ascetic.vmic.api.VmicApi;
import eu.ascetic.vmic.api.datamodel.AbstractProgressData;
import eu.ascetic.vmic.api.datamodel.ProgressDataImage;

/**
 * Core logic of the Virtual Machine Image Constructor (VMIC).
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class VirtualMachineImageConstructor implements Runnable {

    protected static final Logger LOGGER = Logger
            .getLogger(VirtualMachineImageConstructor.class);

    private static final int THREAD_SLEEP_TIME = 250;

    private VmicApi vmicApi;
    private OvfDefinition ovfDefinition;
    private String ovfDefinitionId;

    /**
     * Constructor
     * 
     * @param vmicApi
     *            The initial invocation of the API for accessing global state.
     * @param ovfDefinition
     *            The OVF Definition.
     */
    public VirtualMachineImageConstructor(VmicApi vmicApi,
            OvfDefinition ovfDefinition) {
        this.vmicApi = vmicApi;
        this.ovfDefinition = ovfDefinition;
        this.ovfDefinitionId = ovfDefinition.getVirtualSystemCollection()
                .getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        // Initialise progress...
        vmicApi.getGlobalState().addImageProgress(ovfDefinitionId);

        LOGGER.debug("Added new new progress object, initial progress is: "
                + vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .getCurrentPercentageCompletion());
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // 1) Retrieve Data from OVF
        OvfDefinitionParser ovfDefinitionParser = retriveImageGenerationData();

        // 2) Select mode of image generation
        if ("offline".equals(ovfDefinitionParser.getMode())) {
            // Offline mode
            if (generateImageOffline(ovfDefinitionParser) == false) {
                return;
            }
        } else if ("online".equals(ovfDefinitionParser.getMode())) {
            // Online mode
            if (generateImageOnline(ovfDefinitionParser) == false) {
                return;
            }
        } else {
            // Could not detect mode, so propagate error
            vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                    .setError(true);
            vmicApi.getGlobalState()
                    .getProgressData(ovfDefinitionId)
                    .setException(
                            new ProgressException(
                                    "Could not detect image generation mode!"));
            return;
        }

        // Image generation complete...
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.FINALISE_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                AbstractProgressData.COMPLETED_PERCENTAGE);
        LOGGER.info(vmicApi.getGlobalState().getProgressLogString(
                ovfDefinitionId));

        // Finally add ovfDefinition to progressData object and set completion
        // flag.
        vmicApi.getGlobalState().setOvfDefinition(ovfDefinition);
        vmicApi.getGlobalState().setComplete(ovfDefinitionId);
    }

    /**
     * Gathers the image generation data from the OVF Definition.
     * 
     * @return
     */
    private OvfDefinitionParser retriveImageGenerationData() {
        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.RETRIEVE_DATA_PHASE_ID);
        LOGGER.info("Retriving image generation data from OVF...");

        // TODO: Parse OVF here and see what mode to operate in. Also see what
        // & how to operate over files.
        OvfDefinitionParser ovfDefinitionParser = new OvfDefinitionParser(
                ovfDefinition, vmicApi);
        ovfDefinitionParser.parse();

        // Sleep a little for progress data to update..
        try {
            vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId,
                    AbstractProgressData.COMPLETED_PERCENTAGE);
            Thread.sleep(THREAD_SLEEP_TIME);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info("Retriving of image generation data complete");
        return ovfDefinitionParser;
    }

    /**
     * Method to generate images that mounts the image while it is offline.
     * TODO: refactor progress reporting
     * 
     * @param ovfDefinitionParser
     *            The instance of {@link OvfDefinitionParser} to use in the
     *            image generation process
     * @return False on error to true if image generation successful
     */
    private boolean generateImageOffline(OvfDefinitionParser ovfDefinitionParser) {
        LOGGER.info("Starting offline image generation...");

        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.GENERATE_IMAGES_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId, 0.0);

        // Iterate over every virtual system image
        for (int i = 0; i < ovfDefinitionParser.getImageNumber(); i++) {

            String baseImagePath = ovfDefinitionParser.getBaseImagePath(i);
            String newImageMountPointPath = ovfDefinitionParser
                    .getImageMountPointPath(i);
            String newImagePath = ovfDefinitionParser.getImagePath(i);
            String script = ovfDefinitionParser.getScript(i);

            LOGGER.info("Starting image generation for: " + newImagePath);

            String nbdDevicePath = null;
            try {
                // 1) Copy the correct base image to the VMIC repository
                copyImage(baseImagePath, newImagePath, i,
                        ovfDefinitionParser.getImageNumber());
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 5)
                                        * 1
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);
                // 2) Check nbd module is loaded
                nbdDevicePath = findNextNbdDevice();
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 5)
                                        * 2
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);
                // 3) Mount the image
                mountImage(newImagePath, newImageMountPointPath, nbdDevicePath);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 5)
                                        * 3
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);
                // 4) Add the file to the image(s) using remote a system
                // call
                addfiles(script);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 5)
                                        * 4
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);
                // 5) Unmount the image
                unmountImage(newImageMountPointPath, nbdDevicePath);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 5)
                                        * 5
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);
            } catch (ProgressException e) {

                // Try to clean up the mount point if something failed:
                try {
                    if (nbdDevicePath != null) {
                        LOGGER.info("Cleaning up mount point");
                        unmountImage(newImageMountPointPath, nbdDevicePath);
                    }
                } catch (ProgressException e1) {
                    // Do nothing
                }

                // Propagate the error
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setError(true);
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setException(e);
                return false;
            }
        }

        LOGGER.info("Completed offline image generation...");
        return true;
    }

    /**
     * Method to copy a base image for modification.
     * 
     * @param baseImagePath
     *            Path to the base image
     * @param newImagePath
     *            Path to the copy of the base image
     * @throws ProgressException
     *             Thrown on failure to copy the image
     * @param imageNumber
     *            Used to calculate completion percentage
     * @param totalImages
     *            Used to calculate completion percentage
     */
    private void copyImage(String baseImagePath, String newImagePath,
            int imageNumber, int totalImages) throws ProgressException {
        LOGGER.info("Copying image from " + baseImagePath + " to "
                + newImagePath);

        // Make the directory in case it doesn't exist
        String commandName = "mkdir -p "
                + newImagePath.substring(0, newImagePath.lastIndexOf('/'))
                + ";";
        ArrayList<String> arguments = new ArrayList<String>();
        // Add the rsync command
        arguments.add("rsync -avzPh " + baseImagePath + " " + newImagePath);

        // Construct and invoke system call to rsync
        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), commandName, arguments,
                vmicApi.getGlobalState().getConfiguration());
        Thread thread = new Thread(systemCallRemote);

        try {
            // Execute the system call in a thread
            thread.start();

            while (systemCallRemote.getReturnValue() == -1
                    && !systemCallRemote.isError()) {
                try {
                    // Parse the system call output and set file upload
                    // progress accordingly
                    Vector<String> output = (Vector<String>) systemCallRemote
                            .getOutput();

                    if (output.size() == 0) {
                        Thread.sleep(THREAD_SLEEP_TIME);
                        continue;
                    }

                    String percentage = null;

                    // Work backwards until we find '%'
                    for (int i = output.size() - 1; i >= 0; i--) {
                        String line = output.get(i);
                        line = line.trim();
                        if (line.indexOf('%') != -1) {
                            String[] lineSplit = line.split(" +");

                            try {
                                percentage = lineSplit[1];
                                percentage = percentage.substring(0,
                                        percentage.length() - 1);
                            } catch (IndexOutOfBoundsException e) {
                                throw new SystemCallException(
                                        "Failed to parse rsync output!", e);
                            }
                            break;
                        }
                    }

                    // Set the progress
                    if (percentage != null) {
                        vmicApi.getGlobalState().setProgressPercentage(
                                ovfDefinitionId,
                                ((((100.0 / totalImages) / 5) * 1) * (Double
                                        .parseDouble(percentage) / 100))
                                        + (100.0 / totalImages) * imageNumber);
                    }
                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            if (systemCallRemote.isError()) {
                throw systemCallRemote.getSystemCallException();
            }

            if (systemCallRemote.getReturnValue() != 0) {
                throw new ProgressException("Copying base image failed");
            } else {
                LOGGER.info("Copying complete");
            }

        } catch (SystemCallException e) {
            if (vmicApi.getGlobalState().getConfiguration().isDefaultValues()) {
                LOGGER.warn(
                        "Failed to run command, is this invocation in a unit test?",
                        e);
            } else {
                LOGGER.error("File upload failed!", e);
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setError(true);
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setException(e);
            }
        }
    }

    /**
     * Method to find the next available nbd device's path.
     * 
     * @return The path of next available nbd device
     * @throws ProgressException
     *             Thrown on failure to find an available nbd device
     */
    private String findNextNbdDevice() throws ProgressException {
        LOGGER.info("Finding next available NBD device...");

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        String commandName = "for x in /sys/class/block/nbd*;";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("do S=`cat $x/size`;");
        arguments.add("if [ \"$S\" == \"0\" ];");
        arguments.add("then echo \"/dev/`basename $x`\";");
        arguments.add("break;");
        arguments.add("fi;");
        arguments.add("done");
        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Finding next nbd device failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException("Finding next nbd device failed");
        }

        String nbdDevicePath = systemCallRemote.getOutput().get(1);

        LOGGER.info("Next available nbd device is at: " + nbdDevicePath);

        return nbdDevicePath;
    }

    /**
     * Method to mount an QEMU supported image to the file system.
     * 
     * @param imagePath
     *            The path to the image to be mounted
     * @param mountPoint
     *            The mount point on the file system to use
     * @param nbdDevicePath
     *            The nbd device to use
     * @throws ProgressException
     *             Thrown on failure to mount the image
     */
    private void mountImage(String imagePath, String mountPointPath,
            String nbdDevicePath) throws ProgressException {
        LOGGER.info("Mounting image " + imagePath + " to " + mountPointPath);

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // Make mount point
        String commandName = "mkdir -p " + mountPointPath + ";";
        ArrayList<String> arguments = new ArrayList<String>();
        // Add qemu-nbd command
        arguments.add("sudo qemu-nbd --connect=" + nbdDevicePath + " "
                + imagePath + ";");
        // Check the qemu-nbd command has
        arguments
                .add("while true;do S=`cat /sys/class/block"
                        + nbdDevicePath
                        + "/size`;if [ \"$S\" != \"0\" ];then break;else sleep 1;fi;done;");
        // Add mount command
        // FIXME: Should detect which partition to mount and what file system is
        // in use
        arguments.add("sudo mount -t ext4 " + nbdDevicePath + "p1 "
                + mountPointPath);
        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Mounting the image failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException("Mounting the image failed");
        } else {
            LOGGER.info("Mounting complete");
        }
    }

    /**
     * Adds previously uploaded files to the image.
     * 
     * @param script
     *            The script to execute that adds the files to the image
     * @throws ProgressException
     *             Thrown on failure to execute a script that adds files to the
     *             image
     */
    private void addfiles(String script) throws ProgressException {
        LOGGER.info("Adding files through execution of script...");
        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        String commandName = script;

        try {
            systemCallRemote.runCommand(commandName, null);
        } catch (SystemCallException e) {
            throw new ProgressException("Calling the image script failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException("Calling the image script failed");
        } else {
            LOGGER.info("Files added");
        }
    }

    /**
     * Method to unmount an QEMU supported image from the files system.
     * 
     * @param mountPointPath
     *            The mount point to unmount
     * @param nbdDeviceId
     *            The nbd device Id to disconnect
     * @throws ProgressException
     *             Thrown on failure to unmount the image
     */
    private void unmountImage(String mountPointPath, String nbdDevicePath)
            throws ProgressException {
        LOGGER.info("Unmounting image " + mountPointPath);
        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // Unmount the images file system
        String commandName = "sudo umount " + mountPointPath + ";";
        ArrayList<String> arguments = new ArrayList<String>();
        // Disconnect the nbd device
        arguments.add("sudo nbd-client -d " + nbdDevicePath + ";");
        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Unmounting the image failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException("Unmounting the image failed");
        } else {
            LOGGER.info("Unmounting complete");
        }
    }

    /**
     * Method to generate images using chef while the image is online and
     * booted. TODO: refactor progress reporting
     *
     * @param ovfDefinitionParser
     *            The instance of {@link OvfDefinitionParser} to use in the
     *            image generation process
     * @return False on error to true if image generation successful
     */
    private boolean generateImageOnline(OvfDefinitionParser ovfDefinitionParser) {

        LOGGER.info("Starting online image generation...");

        vmicApi.getGlobalState().setProgressPhase(ovfDefinitionId,
                ProgressDataImage.GENERATE_IMAGES_PHASE_ID);
        vmicApi.getGlobalState().setProgressPercentage(ovfDefinitionId, 0.0);

        // Iterate over every virtual system image
        for (int i = 0; i < ovfDefinitionParser.getImageNumber(); i++) {

            // TODO: Fetch parsed OVF attributes relevant for online image
            // generation .
            String newImagePath = ovfDefinitionParser.getImagePath(i);

            LOGGER.info("Starting image generation for: " + newImagePath);

            String virtualMachineAddress = null;
            try {
                // TODO: 1) Boot up the image and wait for completion (TODO: use
                // libvirt instead of virsh system call)
                virtualMachineAddress = bootImage();
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 1
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

                // TODO: 2) Bootstrap the VM with chef agent via a remote system
                // call
                // to knife.
                bootstrapVirtualMachine(virtualMachineAddress);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 2
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

                // TODO: 3) Upload the chef cookbook(s) to the chef workspace
                // (using rsync)
                uploadCookbooks(virtualMachineAddress);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 3
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

                // TODO: 4) Deploy the chef cookbooks(s) via a remote system
                // call to knife.
                deployCookbooks(virtualMachineAddress);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 4
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

                // TODO: 5) Shutdown the VM (TODO: use libvirt instead of virsh
                // system call)
                shutdownVirtualMachine(virtualMachineAddress);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 5
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

                // TODO: 6) Remove the node from the chef server
                cleanChefServer(virtualMachineAddress);
                vmicApi.getGlobalState()
                        .setProgressPercentage(
                                ovfDefinitionId,
                                ((100.0 / ovfDefinitionParser.getImageNumber()) / 6)
                                        * 6
                                        + (100.0 / ovfDefinitionParser
                                                .getImageNumber()) * i);

            } catch (ProgressException e) {

                // Try to clean up if something failed:
                try {
                    onlineCleanUpOnError(virtualMachineAddress);
                } catch (ProgressException e1) {
                    // Do nothing
                }

                // Propagate the error
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setError(true);
                vmicApi.getGlobalState().getProgressData(ovfDefinitionId)
                        .setException(e);
                return false;
            }
        }

        LOGGER.info("Completed online image generation...");
        return true;
    }

    /**
     * Boots an image and waits for the OS initialisation process to finish.
     * 
     * @return The IP address of the VM created.
     */
    private String bootImage() throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo '1.2.3.4';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");
        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Booting image failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Booting image failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        String virtualMachineAddress = systemCallRemote.getOutput().get(1);

        LOGGER.info("Image booted with IP: " + virtualMachineAddress);

        return virtualMachineAddress;
    }

    /**
     * Install chef client via SHH into a Virtual Machine.
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void bootstrapVirtualMachine(String virtualMachineAddress)
            throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo 'This is a test';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");

        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException(
                    "Bootstrapping VM with chef agent failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Bootstrapping VM failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        LOGGER.info("VM bootstrapped with IP: " + virtualMachineAddress);
    }

    /**
     * Uploads the chef cookbook(s) to the chef workspace
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void uploadCookbooks(String virtualMachineAddress)
            throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo 'This is a test';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");

        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException(
                    "Uploading cookbooks to VM via chef agent failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Uploading cookbooks to VM via chef agent failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        LOGGER.info("Uploaded cookbooks to VM with IP: "
                + virtualMachineAddress);

    }

    /**
     * Deploys chef cookbook(s) to server and agent deployed in a Virtual
     * Machine, installing required software for image.
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void deployCookbooks(String virtualMachineAddress)
            throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo 'This is a test';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");

        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException(
                    "Deploying cookbooks to VM via chef agent failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Deploying cookbooks to VM via chef agent failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        LOGGER.info("Deployed cookbooks to VM with IP: "
                + virtualMachineAddress);

    }

    /**
     * Cleanly shuts down a Virtual Machine.
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void shutdownVirtualMachine(String virtualMachineAddress)
            throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo 'This is a test';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");

        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Shutting down VM failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Shutting down VM failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        LOGGER.info("Shutdown VM with IP: " + virtualMachineAddress);

    }

    /**
     * Removes uploaded cookbook(s) from workspace and server in addition to
     * removing registration of node.
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void cleanChefServer(String virtualMachineAddress)
            throws ProgressException {

        SystemCallRemote systemCallRemote = new SystemCallRemote(
                System.getProperty("user.home"), vmicApi.getGlobalState()
                        .getConfiguration());

        // TODO: Formulate sys call
        String commandName = "echo 'This is a test';";
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add("exit 0");

        try {
            systemCallRemote.runCommand(commandName, arguments);
        } catch (SystemCallException e) {
            throw new ProgressException("Cleaning up chef failed", e);
        }

        if (systemCallRemote.getReturnValue() != 0) {
            throw new ProgressException(
                    "Cleaning up chef failed, return value != 0 instead got: "
                            + systemCallRemote.getReturnValue());
        }

        LOGGER.info("Shutdown VM with IP: " + virtualMachineAddress);

    }

    /**
     * Attempts to clean up after a failure during online image generation.
     * 
     * @param virtualMachineAddress
     *            The IP address of the VM.
     */
    private void onlineCleanUpOnError(String virtualMachineAddress)
            throws ProgressException {
        if (virtualMachineAddress != null) {
            // Try to clean up: shutdown VM and clean chef server
            try {
                shutdownVirtualMachine(virtualMachineAddress);
            } catch (ProgressException e) {
                LOGGER.info("Clean up on error - failed shutdown of VM: "
                        + virtualMachineAddress);
            }

            try {
                cleanChefServer(virtualMachineAddress);
            } catch (ProgressException e) {
                LOGGER.info("Clean up on error - failed clean of chef server for VM: "
                        + virtualMachineAddress);
            }

        } else {
            LOGGER.info("Clean up on error - nothing to clean up");
        }
    }
}
