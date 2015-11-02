package eu.ascetic.saas.applicationpackager.xml.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 * This class implements a application-config node in a XML file
 *
 */

@XmlRootElement(name="application-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationConfig {

	/** The name. */
	@XmlAttribute(name="name")
	private String name;
	
	/** The mode. */
	@XmlAttribute(name="mode")
	private String mode;
	
	/** The chef server url. */
	@XmlAttribute(name="chef-server-url")
	private String chefServerUrl;
	
	/** The nodes. */
	@XmlElement(name="node")
    private ArrayList<Node> nodes;
	
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
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}
 
//	@XmlAttribute
	/**
 * Sets the mode.
 *
 * @param mode the new mode
 */
public void setMode(String mode) {
		this.mode = mode;
	}
	
	/**
	 * Gets the nodes.
	 *
	 * @return the nodes
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	
	/**
	 * Sets the nodes.
	 *
	 * @param nodes the new nodes
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * Adds the node.
	 *
	 * @param node the node
	 */
	public void addNode(Node node) {
		if(nodes == null) nodes = new ArrayList<Node>();
		nodes.add(node);
	}
	
	/**
	 * Gets the chef server url.
	 *
	 * @return the chef server url
	 */
	public String getChefServerUrl() {
		return chefServerUrl;
	}
 
//	@XmlAttribute
	/**
 * Sets the chef server url.
 *
 * @param chefServerUrl the new chef server url
 */
public void setChefServerUrl(String chefServerUrl) {
		this.chefServerUrl = chefServerUrl;
	}
	
}
