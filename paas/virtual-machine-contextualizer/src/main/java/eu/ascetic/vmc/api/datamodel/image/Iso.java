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
package eu.ascetic.vmc.api.datamodel.image;

/**
 * Class for storing the attributes of a ISO disk image.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.3
 */
public class Iso extends Image {

	private String mountPoint;
	private boolean created;

	/**
	 * Constructor for creating a ISO object
	 * 
	 * @param imageId
	 *            The ID of the ISO image (see
	 *            {@link Image#Image(String, String, String, String)} for details).
	 * @param fileName
	 *            The filename of the ISO image as a URI including full system
	 *            path (see {@link Image#Image(String, String, String, String)} for
	 *            details).
	 * @param uri
	 *            The URI of the ISO image as a full system
	 *            path (see {@link Image#Image(String, String, String, String)} for
	 *            details).
	 * @param format
	 *            The format of the ISO image (e.g. iso9660) (see
	 *            {@link Image#Image(String, String, String, String)} for details).
	 * @param mountPoint
	 *            The mount point of the ISO image within a VM.
	 */
	public Iso(String imageId, String fileName, String uri, String format, String mountPoint) {
		super(imageId, fileName, uri, format);
		this.mountPoint = mountPoint;
	}
	
	/**
	 * @return the mountPoint
	 */
	public String getMountPoint() {
		return mountPoint;
	}

	/**
	 * @param mountPoint
	 *            the mountPoint to set
	 */
	public void setMountPoint(String mountPoint) {
		this.mountPoint = mountPoint;
	}

	/**
	 * @return the created
	 */
	public boolean isCreated() {
		return created;
	}

	/**
	 * @param created the created to set
	 */
	public void setCreated(boolean created) {
		this.created = created;
	}

}
