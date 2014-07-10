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

import org.dmtf.schemas.ovf.envelope.x1.XmlBeanFileType;

/**
 * Provides factory methods for creating instances of {@link File}.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class FileFactory {

	/**
	 * Creates a new empty instance of {@link File}.
	 * 
	 * @return The new File instance
	 */
	public File newInstance() {
		return new File(XmlBeanFileType.Factory.newInstance());
	}

	/**
	 * Creates an instance of {@link File} with preset ID and URL.
	 * 
	 * @param id
	 *            The unique ID of the File
	 * @param href
	 *            The URL of the File
	 * @return The new File instance
	 */
	public File newInstance(String id, String href) {
		File file = new File(XmlBeanFileType.Factory.newInstance());
		file.setId(id);
		file.setHref(href);
		return file;
	}
}
