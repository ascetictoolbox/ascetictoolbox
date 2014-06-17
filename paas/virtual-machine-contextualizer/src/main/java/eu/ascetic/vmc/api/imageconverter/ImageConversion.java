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
package eu.ascetic.vmc.api.imageconverter;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import eu.ascetic.vmc.api.core.SystemCall;
import eu.ascetic.vmc.api.core.SystemCallException;
import eu.ascetic.vmc.api.datamodel.GlobalConfiguration;
import eu.ascetic.vmc.api.datamodel.image.HardDisk;

/**
 * Class for converting VM HardDisk images using qemu-img.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.4
 */
public class ImageConversion implements Runnable {

	private static final int THREAD_SLEEP_TIME = 500;

	private static final int KILO_BYTE = 1024;

	protected static final Logger LOGGER = Logger
			.getLogger(ImageConversion.class);

	private String desiredFormat;

	private GlobalConfiguration configuration;
	private SystemCall systemCall;
	private String repository;

	private HardDisk hardDisk;

	/**
	 * Constructor initialises an instance of the Image Conversion tool.
	 * 
	 * @param globalConfiguration
	 *            The VMC global config object to fetch the image repo URI from.
	 * @param desiredFormat The image format to convert to.
	 * @param hardDisk The HardDisk object to convert.
	 */
	public ImageConversion(GlobalConfiguration globalConfiguration,
			String desiredFormat, HardDisk hardDisk) {
		this.configuration = globalConfiguration;
		systemCall = new SystemCall(configuration.getInstallDirectory());
		repository = configuration.getRepository();
		this.desiredFormat = desiredFormat;
		this.hardDisk = hardDisk;
	}

	/**
	 * Method for converting images
	 */
	public void run() {

		FormatDetection formatDetection = new FormatDetection();
		String currentFormat = formatDetection.detect(hardDisk.getUri());

		String commandName = "qemu-img";
		ArrayList<String> arguments = new ArrayList<String>();

		arguments.add("convert");
		if (this.desiredFormat .equals("qcow2")) {
			// Compression on
			arguments.add("-c");
		}
		// Progress
		arguments.add("-p");
		if (currentFormat != null) {
			arguments.add("-fmt");
			arguments.add(currentFormat);
		}
		arguments.add("-O");
		arguments.add(this.desiredFormat);
		if (this.desiredFormat .equals("vmdk")) {
			// Add an option
			arguments.add("-o");
			// SCSI image format (requires patch and qemu-0.15.1)
			arguments.add("scsi");
		}
		arguments.add(this.hardDisk.getUri());
		// TODO: This will fall on its face if there is no extension...
		String newFileName = this.hardDisk.getFileName().substring(0,
				this.hardDisk.getFileName().lastIndexOf("."))
				+ "." + desiredFormat;
		String newUri = repository + "/" + newFileName;
		arguments.add(newUri);

		// Executed command looks like so:
		// qemu-img convert -fmt raw -O qcow2 /path/test.img test.qcow2 -c -p
		try {
			systemCall.runCommand(commandName, arguments);

			while (systemCall.getReturnValue() == -1) {
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
				} catch (InterruptedException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}

		} catch (SystemCallException e) {
			if (configuration.isDefaultValues()) {
				LOGGER.warn(
						"Failed to run command, is this invocation in a unit test?",
						e);
			}
		}

		if (systemCall.getReturnValue() != 0) {
			LOGGER.error("Hardisk conversion Failed! Error code was: "
					+ systemCall.getReturnValue());
		}

		this.hardDisk.setConverted(true);
		this.hardDisk.setFormat(desiredFormat);
		this.hardDisk.setUri(newUri);
		LOGGER.info("Harddisk created with URI: " + this.hardDisk.getUri());
		this.hardDisk.setFileName(newFileName);
		LOGGER.info("Harddisk filename: " + this.hardDisk.getFileName());

		File newImage = new File(this.hardDisk.getUri());
		if (newImage.exists()) {
			LOGGER.info("Harddisk size: " + (newImage.length() / KILO_BYTE / KILO_BYTE)
					+ "MB");
		}
	}

	/**
	 * @return the hardDisk
	 */
	public HardDisk getHardDisk() {
		return hardDisk;
	}

	/**
	 * @return the systemCall
	 */
	public SystemCall getSystemCall() {
		return systemCall;
	}
}
