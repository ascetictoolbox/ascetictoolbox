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
package eu.ascetic.vmc.api.DataModel.ContextDataTypes;

/**
 * Class for storing the attributes associated with a given software dependency.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class SoftwareDependency {

	private String artifactId;
	private String groupId;
	private String version;

	/**
	 * Constructor for creating a SoftwareDependency object.
	 * 
	 * @param artifactId
	 *            The ID of the software dependency.
	 * @param groupId
	 *            The group ID of the software dependency.
	 * @param version
	 *            The version of the software dependency.
	 */
	public SoftwareDependency(String artifactId, String groupId, String version) {
		this.artifactId = artifactId;
		this.groupId = groupId;
		this.version = version;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId
	 *            the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

}
