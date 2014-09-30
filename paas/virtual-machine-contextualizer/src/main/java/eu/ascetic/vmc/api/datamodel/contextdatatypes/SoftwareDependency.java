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
package eu.ascetic.vmc.api.datamodel.contextdatatypes;

/**
 * Class for storing the attributes associated with a given software dependency.
 * 
 * @author Django Armstrong (ULeeds)
 * @version 0.0.2
 */
public class SoftwareDependency {

    private String id;
    private String type;
    private String packageUri;
    private String installScriptUri;

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
    public SoftwareDependency(String id, String type, String packageUri, String installScriptUri) {
        this.id = id;
        this.type = type;
        this.packageUri = packageUri;
        this.installScriptUri = installScriptUri;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the packageUri
     */
    public String getPackageUri() {
        return packageUri;
    }

    /**
     * @param packageUri the packageUri to set
     */
    public void setPackageUri(String packageUri) {
        this.packageUri = packageUri;
    }

    /**
     * @return the installScriptUri
     */
    public String getInstallScriptUri() {
        return installScriptUri;
    }

    /**
     * @param installScriptUri the installScriptUri to set
     */
    public void setInstallScriptUri(String installScriptUri) {
        this.installScriptUri = installScriptUri;
    }

}
