package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

// TODO: Auto-generated Javadoc
/**
 * 
 * Copyright 2015 ATOS SPAIN S.A. 
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author David Rojo Antona. Atos Research and Innovation, Atos SPAIN SA
 * e-mail david.rojoa@atos.net 
 * 
 * This class implements a base-dependency node in a XML file
 *
 */
@XmlRootElement(name ="base-dependency")
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseDependency {

	/** The os. */
	@XmlAttribute(name="OS")
	private String os;

	/** The os version. */
	@XmlAttribute(name="OSVersion")
	private String osVersion;
	
	/**
	 * Gets the os.
	 *
	 * @return the os
	 */
	public String getOs() {
		return os;
	}

//	@XmlAttribute
	/**
	 * Sets the os.
	 *
	 * @param os the new os
	 */
	public void setOs(String os) {
		this.os = os;
	}

	/**
	 * Gets the os version.
	 *
	 * @return the os version
	 */
	public String getOsVersion() {
		return osVersion;
	}

	/**
	 * Sets the os version.
	 *
	 * @param osVersion the new os version
	 */
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

}
