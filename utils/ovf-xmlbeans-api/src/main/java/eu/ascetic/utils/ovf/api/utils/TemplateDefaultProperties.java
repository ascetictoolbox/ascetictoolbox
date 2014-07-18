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
package eu.ascetic.utils.ovf.api.utils;

import java.io.IOException;
import java.util.Properties;

import eu.ascetic.utils.ovf.api.exceptions.OvfRuntimeException;

/**
 * A class to define attributes for a default {@link Properties} object used in
 * {@link TemplateLoader}. The properties in turn define OVF variables to be
 * loaded into a template.
 * 
 * @author Django Armstrong (ULeeds)
 * 
 */
public class TemplateDefaultProperties extends Properties {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -5920612312069101940L;

	/**
	 * Location of the default properties file relative to the class path.
	 */
	private static final String DEFAULT_PROPERTIES_FILE = "/ovf.properties";

	/**
	 * Creates a properties object that contains all default values for an OVF
	 * definition loaded from the DEFAULT_PROPERTIES_FILE file.
	 */
	public TemplateDefaultProperties() {
		try {
			this.load(this.getClass().getResourceAsStream(
					DEFAULT_PROPERTIES_FILE));
		} catch (IOException e) {
			throw new OvfRuntimeException("Loading default properties failed.", e);
		}
	}
}
