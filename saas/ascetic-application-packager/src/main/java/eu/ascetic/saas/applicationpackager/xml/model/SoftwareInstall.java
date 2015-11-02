package eu.ascetic.saas.applicationpackager.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
 * This class implements a software_install node in a XML file
 *
 */
@XmlRootElement(name ="software_install")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoftwareInstall {

	/** The name. */
	@XmlAttribute(name="name")
	private String name;
	
	/** The chef uri. */
	@XmlAttribute(name="chefURI")
	private String chefUri;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

//	@XmlAttribute
	/**
 * Sets the name.
 *
 * @param name the new name
 */
public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the chef uri.
	 *
	 * @return the chef uri
	 */
	public String getChefUri() {
		return chefUri;
	}

//	@XmlAttribute
	/**
 * Sets the chef uri.
 *
 * @param chefUri the new chef uri
 */
public void setChefUri(String chefUri) {
		this.chefUri = chefUri;
	}
	
	
}
