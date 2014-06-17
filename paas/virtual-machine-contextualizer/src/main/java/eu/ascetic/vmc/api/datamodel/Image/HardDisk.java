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
package eu.ascetic.vmc.api.DataModel.Image;

/**
 * Class for storing the attributes of a virtual Hard Disk Drive
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.4
 */
public class HardDisk extends Image {

	private String diskCapacity;
	private String diskFormatString;
	// TODO: use fileNameId when there is support for multiple disk in the
	// Manifest API
	private String fileNameId;
	private boolean converted;

	/**
	 * Constructor for creating a HardDisk object
	 * 
	 * @param imageId
	 *            The id of the hard disk image taken from the manifest as
	 *            diskId (see {@link Image#Image(String, String, String, String)} for
	 *            details).
	 * @param fileName
	 *            The filename of the ISO image as a URI including full system
	 *            path (see {@link Image#Image(String, String, String, String)} for
	 *            details).
	 * @param format
	 *            The format of the disk image (see
	 *            {@link Image#Image(String, String, String, String)} for details).
	 * @param diskCapacity
	 *            The capacity of the disk image.
	 * @param diskFormatString
	 *            The format string taken from the manifest.
	 */
	public HardDisk(String imageId, String fileName, String uri, String format,
			String diskCapacity, String diskFormatString/* , String fileNameId */) {
		super(imageId, fileName, uri, format);

		this.diskCapacity = diskCapacity;
		this.diskFormatString = diskFormatString;
		// TODO: use fileNameId when there is support for multiple disk in the
		// Manifest API "this.fileNameId = fileNameId;"
		this.converted = false;
	}

	/**
	 * @return the diskCapacity
	 */
	public String getDiskCapacity() {
		return diskCapacity;
	}

	/**
	 * @param diskCapacity
	 *            the diskCapacity to set
	 */
	public void setDiskCapacity(String diskCapacity) {
		this.diskCapacity = diskCapacity;
	}

	/**
	 * @return the diskFormatString
	 */
	public String getDiskFormatString() {
		return diskFormatString;
	}

	/**
	 * @param diskFormatString
	 *            the diskFormatString to set
	 */
	public void setDiskFormatString(String diskFormatString) {
		this.diskFormatString = diskFormatString;
	}

	/**
	 * @return the fileNameId
	 */
	public String getFileNameId() {
		return fileNameId;
	}

	/**
	 * @param fileNameId
	 *            the fileNameId to set
	 */
	public void setFileNameId(String fileNameId) {
		this.fileNameId = fileNameId;
	}

	/**
	 * @return the converted
	 */
	public boolean isConverted() {
		return converted;
	}

	/**
	 * @param converted the converted to set
	 */
	public void setConverted(boolean converted) {
		this.converted = converted;
	}

}
