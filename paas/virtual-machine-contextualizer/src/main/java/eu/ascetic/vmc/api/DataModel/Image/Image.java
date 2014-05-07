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
 * Class for defining inheritable attributes of an image.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.3
 */
public class Image {

	private String imageId;
	private String fileName;
	private String uri;
	private String format;

	/**
	 * Default constructor inherited by {@link Iso}, {@link HardDisk}
	 * 
	 * @param imageId
	 *            The ID of the image.
	 * @param fileName
	 *            The filename of the image.
	 * @param uri
	 *            The URI of the image.
	 * @param format
	 *            The format of the image.
	 */
	public Image(String imageId, String fileName, String uri, String format) {
		this.imageId = imageId;
		this.fileName = fileName;
		this.uri = uri;
		this.format = format;
	}

	/**
	 * @return the id
	 */
	public String getImageId() {
		return imageId;
	}

	/**
	 * @param imageId
	 *            the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	/**
	 * @return the fileName (URI format)
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set (URI format)
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format
	 *            the format to set
	 */
	public void setFormat(String format) {
		this.format = format;
	}
}
