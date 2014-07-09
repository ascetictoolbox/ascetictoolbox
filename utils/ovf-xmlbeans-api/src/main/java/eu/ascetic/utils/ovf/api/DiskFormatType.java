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
package eu.ascetic.utils.ovf.api;

/**
 * Enumeration containing predefined constants of supported disk format types.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public enum DiskFormatType {

	// @formatter:off
	VMDK("http://www.vmware.com/interfaces/specifications/vmdk.html#streamOptimized"),
	VHD("http://technet.microsoft.com/en-us/library/bb676673.aspx"),
	QCOW2("http://www.gnome.org/~markmc/qcow-image-format.html");
	// TODO: Add others here?
	// @formatter:on

	/**
	 * A URL to the disk format specification.
	 */
	private String specificationUrl;

	/**
	 * Default constructor.
	 * 
	 * @param formatSpecificationUrl
	 *            The URL defining the disk format
	 */
	private DiskFormatType(String formatSpecificationUrl) {
		this.specificationUrl = formatSpecificationUrl;
	}

	/**
	 * Returns the URL to the disk format specification.
	 * 
	 * @return The URL to the disk format specification.
	 */
	public String getSpecificationUrl() {
		return specificationUrl;
	}

	/**
	 * Finds the enumeration object representation of the specification URL.
	 * 
	 * @param specificationUrl
	 *            The URL
	 * @return A DiskFormatType enumeration object for the specific URL
	 */
	public static DiskFormatType findBySpecificationUrl(String specificationUrl) {
		if (specificationUrl != null) {
			for (DiskFormatType df : DiskFormatType.values()) {
				if (df.getSpecificationUrl().equals(specificationUrl)) {
					return df;
				}
			}
		}
		throw new IllegalArgumentException(
				"There is no disk getSpecificationUrl with getSpecificationUrl '"
						+ specificationUrl + "' specified.");
	}
}
