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

	/** The application name. */
	@XmlAttribute(name="applicationName")
	private String applicationName;
	
	/** The deployment name. */
	@XmlAttribute(name="deploymentName")
	private String deploymentName;
	
	/** The mode. */
	@XmlAttribute(name="deploymentMode")
	private String deploymentMode;
	
	/** The chef server url. */
	@XmlAttribute(name="chef-server-url")
	private String chefServerUrl;
	
	/** The application sla info. */
	@XmlElement(name="applicationSLAInfo")
    private ApplicationSlaInfo applicationSLAInfo;
	
	/** The nodes. */
	@XmlElement(name="node")
    private ArrayList<Node> nodes;
	
	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	public String getApplicationName() {
		return applicationName;
	}

	//	@XmlAttribute
	/**
	 * Sets the application name.
	 *
	 * @param applicationName the new application name
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Gets the deploymentName name.
	 *
	 * @return the deploymentName name
	 */
	public String getDeploymentName() {
		return deploymentName;
	}

	//@XmlAttribute
	/**
	 * Sets the deploymentName name.
	 *
	 * @param deploymentName the new deployment name
	 */
	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
	
	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public String getDeploymentMode() {
		return deploymentMode;
	}
 
	//	@XmlAttribute
	/**
	 * Sets the mode.
	 *
	 * @param mode the new mode
	 */
	public void setDeploymentMode(String deploymentMode) {
		this.deploymentMode = deploymentMode;
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

	/**
	 * Gets the application sla info.
	 *
	 * @return the application sla info
	 */
	public ApplicationSlaInfo getApplicationSLAInfo() {
		return applicationSLAInfo;
	}

	/**
	 * Sets the application sla info.
	 *
	 * @param applicationSLAInfo the new application sla info
	 */
	public void setApplicationSLAInfo(ApplicationSlaInfo applicationSLAInfo) {
		this.applicationSLAInfo = applicationSLAInfo;
	}
	
	
}
